package com.indramind.cybersec.secure_tasks_api.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;

@Service
public class JwtService{

	private final SecretKey signingKey;
	private final long accessTokenExpirationMs;

	private static final Logger log = LoggerFactory.getLogger(JwtService.class);

	public JwtService(@Value("${spring.secrets.jwt.secretkey}") String secretKey,
						@Value("${spring.secrets.jwt.expiration-ms:900000}") long accessTokenExpirationMs) {
		this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs; // default 15 min
	}

	public String generateAccessToken(UserDetails user){
		Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

		return Jwts.builder().setSubject(user.getUsername()) // Is really email
		.setIssuedAt(now)
		.setExpiration(expiry)
		.signWith(signingKey, SignatureAlgorithm.HS256)
		.compact();
	}

	public boolean isTokenValid(String token) {
		return parseClaims(token) != null;
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String email = extractEmail(token);
		return email != null && !email.equals("") && email.equals(userDetails.getUsername()) && isTokenValid(token);
	}

	public String extractEmail(String token) {
        Claims claims = parseClaims(token);
		return claims == null? null:claims.getSubject();
    }

    public Date extractExpiration(String token) {
		Claims claims = parseClaims(token);
		return claims == null? null:claims.getExpiration();
    }

	public Claims parseClaims(String token) {
		try {
			return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();

		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			log.warn("JWT expired: correlationId={}", MDC.get("correlationId"));
		} catch (io.jsonwebtoken.security.SignatureException e) {
			log.warn("JWT signature invalid (possible tampering): correlationId={}", MDC.get("correlationId"));
		} catch (JwtException e) {
			log.warn("JWT invalid: error: {}, correlationId={}", e.getClass().getSimpleName(), MDC.get("correlationId"));
		}
		
		return null;
	}
}
