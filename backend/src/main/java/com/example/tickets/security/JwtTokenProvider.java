package com.example.tickets.security;

import com.example.tickets.config.JwtConfig;
import com.example.tickets.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final JwtConfig jwtConfig;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String createToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtConfig.getExpirationMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
            .subject(user.getEmail())
            .claim("userId", user.getId())
            .claim("role", user.getRole().name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(key())
            .compact();
    }

    public String getSubject(String token) {
        return claims(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            claims(token);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private Claims claims(String token) {
        return Jwts.parser()
            .verifyWith(key())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}

