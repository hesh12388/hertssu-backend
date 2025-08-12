package com.hertssu.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import com.hertssu.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final Key refreshTokenKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Token expires in 1 hour
    private final long expirationMs = 3600000;

    // Refresh token expires in 7 days
    private final long refreshTokenExpirationMs = 604800000;

    //generate JWT token for user
    public String generateToken(User user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

      
        Integer committeeId = null;
        String committeeSlug = null;
        if (user.getCommittee() != null) {
            committeeId = user.getCommittee().getId();
            committeeSlug = user.getCommittee().getSlug();
        }

        Integer subcommitteeId = null;
        String subcommitteeSlug = null;
        if (user.getSubcommittee() != null) {
            subcommitteeId = user.getSubcommittee().getId();
            subcommitteeSlug = user.getSubcommittee().getSlug();
        }

        String displayName = (user.getFirstName() + " " + user.getLastName()).trim();

        JwtBuilder builder = Jwts.builder()
            .setId(java.util.UUID.randomUUID().toString())
            .setSubject(user.getEmail())
            .setIssuedAt(now)
            .setExpiration(exp)
            .claim("uid", user.getId())
            .claim("email", user.getEmail())
            .claim("name", displayName)
            .claim("role", user.getRole());

        if (committeeId != null)        builder.claim("committeeId", committeeId);
        if (committeeSlug != null)      builder.claim("committee", committeeSlug);

        if (subcommitteeId != null)     builder.claim("subcommitteeId", subcommitteeId);
        if (subcommitteeSlug != null)   builder.claim("subcommittee", subcommitteeSlug);

        return builder
            .signWith(key)
            .compact();
    }


    // Validate JWT token
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid or expired JWT", e);
        }
    }

    // Generate refresh token for user
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(refreshTokenKey)
                .compact();
    }

    // Validate refresh token
    public Claims validateRefreshToken(String token) {
         try {
            return Jwts.parserBuilder()
                    .setSigningKey(refreshTokenKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid or expired JWT", e);
        }
    }
}
