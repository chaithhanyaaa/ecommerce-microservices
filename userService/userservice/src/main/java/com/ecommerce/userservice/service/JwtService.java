package com.ecommerce.userservice.service;

import com.ecommerce.userservice.config.JwtConfig;
import com.ecommerce.userservice.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService
{
    private final JwtConfig jwtConfig;

    public JwtService(JwtConfig jwtConfig)
    {
        this.jwtConfig = jwtConfig;
    }


    private SecretKey getSigningKey()
    {
        //JWT cannot sign using a String,so we need to convert that to bytes
        return Keys.hmacShaKeyFor(
                jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }


    public String generateToken(User user)
    {
        return Jwts.builder()

                .subject(user.getEmail())

                .claim("userId", user.getId())

                .claim("role", user.getRole().name())

                .issuedAt(new Date())

                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + jwtConfig.getExpiration()
                        )
                )

                .signWith(getSigningKey(), SignatureAlgorithm.HS256)

                .compact();
    }


    private Claims extractAllClaims(String token)
    {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public String extractEmail(String token)
    {
        return Jwts.parser()

                .verifyWith(getSigningKey())

                .build()

                .parseSignedClaims(token)

                .getPayload()

                .getSubject();
    }


    public Long extractUserId(String token)
    {
        Claims claims = Jwts.parser()

                .verifyWith(getSigningKey())

                .build()

                .parseSignedClaims(token)

                .getPayload();

        return claims.get("userId", Long.class);
    }

    public String extractRole(String token)
    {
        Claims claims = Jwts.parser()

                .verifyWith(getSigningKey())

                .build()

                .parseSignedClaims(token)

                .getPayload();

        return claims.get("role", String.class);
    }


    public boolean isTokenValid(String token)
    {
        try
        {
            extractAllClaims(token);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

}