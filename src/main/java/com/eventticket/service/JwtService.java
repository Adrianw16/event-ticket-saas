package com.eventticket.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey Key;
    private final long expirationMs;

    public JwtService(
            @Value ("${jwt.secret:my-256-bit-secret-must-be-at-least-32-characters-long}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs
    ){
        //ensure secret is at least 32 characters long
        if(secret.length() < 32){
            throw new IllegalArgumentException("Secret must be at least 32 characters long");
        }
        //create signing key from secret
        this.Key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a JWT token for the user.
     * Claims: sub(user ID), org_id(tenant), email, iat, exp.
     */
    public String generateToken(Long userId, Long orgId, String email) {
        Instant now = Instant.now();
        Date expiration = Date.from(now.plusMillis(expirationMs));

        return Jwts.builder()
                //Claims for the token
                .subject(userId.toString())
                .claim("org_id", orgId)
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(expiration)
                .signWith(Key)
                .compact();
    }

    /**
     * Parse and validate JWT token. Returns claim if valid.
     * Throws JWT exception (or subclass) if invalide/expired/tampered.
     */
    public Claims validateToken(String token){
        try{
            return Jwts.parser()
                    .verifyWith(Key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e){
            throw new JwtException("Expired token", e);
        } catch (JwtException e){
            throw new JwtException("Invalid token: " + e.getMessage(), e);
        }
    }

    /**
     * Extract userId from valid token.
     */
    public Long extractUserId(String token){
        Claims claims = validateToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extract org_id from a valid token.
     */
    public Long extractOrgId(String token){
        Claims claims = validateToken(token);
        return claims.get("org_id", Long.class);
    }
}
