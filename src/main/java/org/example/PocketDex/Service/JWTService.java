package org.example.PocketDex.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.PocketDex.Config.JWTConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JWTService {


    private final SecretKey key;
    private final SecretKey supabaseKey;
    private final int BACKEND_TOKEN_DURATION = 900; // backend issued tokens have a duration of 15 mins

    @Autowired
    public JWTService(JWTConfig config) {
        this.key = Keys.hmacShaKeyFor(config.getJwtSecretKey().getBytes());
        this.supabaseKey = Keys.hmacShaKeyFor(config.getSupabaseSecretKey().getBytes());
    }

    public String extractToken(String jwtToken) {
        String prefix = "Bearer ";

        if (!jwtToken.contains(prefix) || !jwtToken.startsWith(prefix)) {
            throw new IllegalArgumentException("JWT Token given Not Valid: toke should start with 'Bearer '");
        }

        return jwtToken.substring(prefix.length());
    }

    public String generateTokenFromSessionId(String sessionId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(BACKEND_TOKEN_DURATION);

        try {

            return Jwts.builder()
                    .issuer("PocketDex-Backend")
                    .subject(sessionId)
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(exp))
                    .signWith(this.key)
                    .compact();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw e;
        }
    }

    public Claims getPayloadFromToken(String backendToken) {
        try {
            return Jwts.parser()
                    .verifyWith(this.key)
                    .build()
                    .parseSignedClaims(backendToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (JwtException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid JWT", e);
        }
    }

    public String getUserIdFromToken(String jwt) {
        try {
            return Jwts
                    .parser()
                    .verifyWith(this.supabaseKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload()
                    .getSubject();

        } catch (ExpiredJwtException e) {
            return e.getClaims()
                    .getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw e;
        }
    }

}
