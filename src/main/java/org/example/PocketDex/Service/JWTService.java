package org.example.PocketDex.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.PocketDex.Config.JWTConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.UUID;

@Service
public class JWTService {


    private final SecretKey key;

    @Autowired
    public JWTService(JWTConfig config) {
        this.key = Keys.hmacShaKeyFor(config.getJwtSecretKey().getBytes());
    }

    public String extractUserToken (String jwtToken) {
        String prefix = "Bearer ";

        if (!jwtToken.contains(prefix) || !jwtToken.startsWith(prefix)) {
            throw new IllegalArgumentException("JWT Toke given Not Valid: toke should start with 'Bearer '");
        }

        return jwtToken.substring(prefix.length());
    }

    public String getUserIdFromToken(String jwt) {
        try {
            Claims claims = Jwts
                    .parser()
                    .verifyWith(this.key)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();

            String idFromJWT = claims.getSubject();

            return idFromJWT;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw e;
        }
    }

}
