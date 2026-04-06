package com.indramind.cybersec.secure_tasks_api.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import com.indramind.cybersec.secure_tasks_api.entity.AppUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;

@Service
public class JwtService{

	private final SecretKey signingKey;
	private final long accessTokenExpirationMs;

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
		try {
			Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
			return true;
		} catch (JwtException e) {
			return false;
		}
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String email = extractEmail(token);
		return email != null && email != "" && email.equals(userDetails.getUsername()) && isTokenValid(token);
	}

	public String extractEmail(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    public Date extractExpiration(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
            return claims.getExpiration();
        } catch (JwtException e) {
            return null;
        }
    }
}
