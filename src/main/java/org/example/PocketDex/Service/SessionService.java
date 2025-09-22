package org.example.PocketDex.Service;

import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.UnifiedJedis;

public class TokenService {

    private final UnifiedJedis jedis;

    @Autowired
    public TokenService(UnifiedJedis jedis) {
        this.jedis = jedis;
    }

    public void saveToken(String key, String value) {
        jedis.setex(key, 900, value);
    }

    public String getToken(String key) {
        return jedis.get(key);
    }
}
