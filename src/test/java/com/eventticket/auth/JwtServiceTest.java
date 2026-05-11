package com.eventticket.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private String jwtSecret = "my-256-bit-secret-key-must-be-at-least-32-characters-long";
    private SecretKey key;

    @BeforeEach
    void setUp() {
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @Test
    void testGenerateToken() {
        Instant now = Instant.now();
        Date expiration = Date.from(now.plusSeconds(3600));

        String token = Jwts.builder()
                .subject("user123")
                .issuedAt(Date.from(now))
                .expiration(expiration)
                .claim("email", "organizer@example.com")
                .claim("org_id", 42L)
                .signWith(key)
                .compact();

        assertNotNull(token);
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts");
    }

    @Test
    void testValidateToken() {
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject("user123")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .claim("email", "organizer@example.com")
                .claim("org_id", 42L)
                .signWith(key)
                .compact();

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("user123", claims.getSubject());
        assertEquals("organizer@example.com", claims.get("email", String.class));
        assertEquals(42, claims.get("org_id", Long.class));
    }

    @Test
    void testExpiredToken() {
        Instant now = Instant.now();
        String expiredToken = Jwts.builder()
                .subject("user123")
                .issuedAt(Date.from(now.minusSeconds(7200)))
                .expiration(Date.from(now.minusSeconds(3600)))
                .signWith(key)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(expiredToken);
        });
    }

    @Test
    void testInvalidSignature() {
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject("user123")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(key)
                .compact();

        SecretKey wrongKey = Keys.hmacShaKeyFor("wrong-secret-key-must-be-long-enough-more-than-32-bytes".getBytes());

        assertThrows(SignatureException.class, () -> {
            Jwts.parser()
                    .verifyWith(wrongKey)
                    .build()
                    .parseSignedClaims(token);
        });
    }
}