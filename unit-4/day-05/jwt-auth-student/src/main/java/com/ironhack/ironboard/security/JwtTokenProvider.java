package com.ironhack.ironboard.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

// Generates, validates, and reads JWT tokens.
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String email, String role) {
        return JWT.create()
                .withSubject(email)
                .withClaim("role", role)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusMillis(expiration))
                .sign(Algorithm.HMAC256(secret));
    }

    public boolean validateToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        DecodedJWT decoded = JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);
        return decoded.getSubject();
    }

    public String getRoleFromToken(String token) {
        DecodedJWT decoded = JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);
        return decoded.getClaim("role").asString();
    }
}
