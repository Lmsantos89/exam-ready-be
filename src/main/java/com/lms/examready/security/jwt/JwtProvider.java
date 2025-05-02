package com.lms.examready.security.jwt;


import com.lms.examready.model.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.*;

import static io.jsonwebtoken.Jwts.SIG.HS256;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;


@Component
@Slf4j
public class JwtProvider {

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    @Value("${jwt.expiration-in-millis}")
    private Long JWT_EXPIRATION;

    public static final String AUTH_TOKEN_TYPE = "Bearer ";
    public static final String ROLE_PREFIX = "ROLE_";

    public String generateToken(UUID userId, String username, Role role) {

        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSigningKey(), HS256)
                .compact();
    }

    public Authentication getAuthentication(ServerHttpRequest request) {
        String token = extractToken(request);
        if (token != null) {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes()))
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();


                String userId = claims.getSubject();
                String username = claims.get("username", String.class);
                String role = claims.get("role", String.class);

                if (userId != null && username != null && role != null) {
                    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role));
                    return new UsernamePasswordAuthenticationToken(
                            userId, null, authorities
                    );
                }
            } catch (ExpiredJwtException e) {
                log.warn("Expired token: {}", e.getMessage());
                return null;
            } catch (SignatureException | MalformedJwtException e) {
                log.warn("Invalid token: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }

    public boolean validateToken(ServerHttpRequest request) {
        String token = extractToken(request);
        if (token != null) {
            try {
                Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
                return true;
            } catch (ExpiredJwtException e) {
                log.warn("Expired token: {}", e.getMessage());
                return false;
            } catch (SignatureException | MalformedJwtException e) {
                log.warn("Invalid token :{}", e.getMessage());
                return false;
            }
        }
        return false;
    }

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AUTH_TOKEN_TYPE)) {
            String token = bearerToken.substring(AUTH_TOKEN_TYPE.length());
            if (StringUtils.hasText(token)) {
                return token;
            }

        }
        return null;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = hexToBytes(JWT_SECRET);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes for HS256");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
