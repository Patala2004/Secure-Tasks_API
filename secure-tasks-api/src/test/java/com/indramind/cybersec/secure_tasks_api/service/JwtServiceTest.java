package com.indramind.cybersec.secure_tasks_api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.indramind.cybersec.secure_tasks_api.security.JwtService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails user;

    private final static String SECRET = "my-super-secret-key-my-super-secret-key";
    private final static long EXPIRATION = 1000 * 60; // 1 minute

    @BeforeEach
    void setup() {
        jwtService = new JwtService(SECRET, EXPIRATION);

        user = User.builder()
                .username("test@email.com")
                .password("password")
                .authorities("USER")
                .build();
    }

    @Test
    void generateAccessToken_shouldReturnValidToken() {
        String token = jwtService.generateAccessToken(user);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalse_forInvalidToken() {
        String invalidToken = "invalid.token.value";

        assertFalse(jwtService.isTokenValid(invalidToken));
    }

    @Test
    void isTokenValid_withUser_shouldReturnTrue_whenMatchingUser() {
        String token = jwtService.generateAccessToken(user);

        boolean result = jwtService.isTokenValid(token, user);

        assertTrue(result);
    }

    @Test
    void isTokenValid_withUser_shouldReturnFalse_whenUserMismatch() {
        String token = jwtService.generateAccessToken(user);

        UserDetails otherUser = User.builder()
                .username("other@email.com")
                .password("password")
                .authorities("USER")
                .build();

        boolean result = jwtService.isTokenValid(token, otherUser);

        assertFalse(result);
    }

    @Test
    void extractEmail_shouldReturnCorrectEmail() {
        String token = jwtService.generateAccessToken(user);

        String email = jwtService.extractEmail(token);

        assertEquals("test@email.com", email);
    }

    @Test
    void extractEmail_shouldReturnNull_forInvalidToken() {
        String invalidToken = "invalid.token";

        String email = jwtService.extractEmail(invalidToken);

        assertNull(email);
    }

    @Test
    void extractExpiration_shouldReturnFutureDate() {
        String token = jwtService.generateAccessToken(user);

        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void extractExpiration_shouldReturnNull_forInvalidToken() {
        String invalidToken = "invalid.token";

        Date expiration = jwtService.extractExpiration(invalidToken);

        assertNull(expiration);
    }

    @Test
    void token_shouldExpire() throws InterruptedException {
        Date now = new Date();
        Date expiry = new Date(now.getTime() - 1000); // Create expired token

		String token =  Jwts.builder().setSubject(user.getUsername()) // Is really email
		.setIssuedAt(now)
		.setExpiration(expiry)
		.signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
		.compact();


        assertFalse(jwtService.isTokenValid(token));
    }
}