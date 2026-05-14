package com.indramind.cybersec.secure_tasks_api.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.indramind.cybersec.secure_tasks_api.logging.CustomLogger;
import com.indramind.cybersec.secure_tasks_api.logging.impl.CustomLoggerFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;

@Service
public class JwtService{

	private final SecretKey signingKey;
	private final long accessTokenExpirationMs;

	private static final CustomLogger log = CustomLoggerFactory.getLogger(JwtService.class);

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
		return parseClaims(token).isPresent();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String email = extractEmail(token);
		return email != null && !email.equals("") && email.equals(userDetails.getUsername()) && isTokenValid(token);
	}

	public String extractEmail(String token) {
        return parseClaims(token)
            .map(Claims::getSubject)
            .orElse(null);
    }

    public Date extractExpiration(String token) {
		return parseClaims(token)
            .map(Claims::getExpiration)
            .orElse(null);
    }

	public Optional<Claims> parseClaims(String token) {
		try {
			return Optional.of(Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody());

		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			log.warn("JWT expired. Expiration: {}, Token created at: {}", e.getClaims().getExpiration(), e.getClaims().getIssuedAt());
		} catch (io.jsonwebtoken.security.SignatureException e) {
			log.warn("JWT signature invalid (possible tampering)");
		} catch (JwtException e) {
			log.warn("JWT invalid: error: {}", e.getClass().getSimpleName());
		}
		
		return Optional.empty();
	}
}
