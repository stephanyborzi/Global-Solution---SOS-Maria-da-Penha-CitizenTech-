package com.citizentech.sos.security;

import com.citizentech.sos.model.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret:}") String secret,
                      @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        if (secret == null || secret.isBlank()) {
            this.key = Jwts.SIG.HS256.key().build();
            log.warn("JWT_SECRET is not set: using a random secret for this run (tokens become invalid on restart).");
        } else {
            byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                throw new IllegalStateException("JWT_SECRET must have at least 32 characters");
            }
            this.key = Keys.hmacShaKeyFor(bytes);
        }
        this.expirationMs = expirationMs;
    }

    public long getExpirationMs() { return expirationMs; }

    public String generate(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("role", user.getProfile().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public Optional<Long> parseUserId(String token) {
        try {
            String subject = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload().getSubject();
            return Optional.of(Long.parseLong(subject));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
