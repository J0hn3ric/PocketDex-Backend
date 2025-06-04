package org.example.PocketDex.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JWTConfig {

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    public String getJwtSecretKey() {
        return jwtSecretKey;
    }
}
