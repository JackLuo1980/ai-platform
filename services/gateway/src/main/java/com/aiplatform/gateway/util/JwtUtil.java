package com.aiplatform.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class JwtUtil {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtil(String secret, long accessTokenExpirationMs, long refreshTokenExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpirationMs;
        this.refreshTokenExpiration = refreshTokenExpirationMs;
    }

    public String generateAccessToken(Long userId, Long tenantId, List<String> roles) {
        return buildToken(userId, tenantId, roles, accessTokenExpiration);
    }

    public String generateRefreshToken(Long userId, Long tenantId) {
        return buildToken(userId, tenantId, null, refreshTokenExpiration);
    }

    private String buildToken(Long userId, Long tenantId, List<String> roles, long expiration) {
        long now = System.currentTimeMillis();
        var builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration))
                .claims(Map.of("tenantId", tenantId));
        if (roles != null && !roles.isEmpty()) {
            builder.claims(Map.of("roles", String.join(",", roles)));
        }
        return builder.signWith(key).compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validate(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }

    public Long getTenantId(String token) {
        return parseToken(token).get("tenantId", Long.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        String rolesStr = parseToken(token).get("roles", String.class);
        if (rolesStr == null || rolesStr.isEmpty()) {
            return List.of();
        }
        return List.of(rolesStr.split(","));
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
