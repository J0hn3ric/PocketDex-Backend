package org.example.PocketDex.Service;

import org.example.PocketDex.Utils.UserConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.time.Instant;

@Service
public class UserMonitorService {

    private final UnifiedJedis jedis;
    private final UserService userService;

    @Autowired UserMonitorService(
            UnifiedJedis jedis,
            UserService userService
    ) {
        this.jedis = jedis;
        this.userService = userService;
    }

    public void checkAndDeleteInactiveUsers() {
        long now = Instant.now().getEpochSecond();
        long threshold = now - (30L * 24 * 60 * 60);

        // scan for every user in redis
        String cursor = ScanParams.SCAN_POINTER_START;
        ScanParams scanParams = new ScanParams().match("user_session:*").count(100);

        do {
            ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
            for (String key : scanResult.getResult()) {
                String lastActiveStr = jedis.hget(key, UserConstants.LAST_ACTIVE_KEY);
                if (lastActiveStr == null) { continue; }

                long lastActive = Long.parseLong(lastActiveStr);
                if (lastActive < threshold) {
                    String userId = key.replace("user_session:", "");

                    // delete user from database
                    userService.deleteUserUsingUserId(userId);
                    // delete tokens
                    jedis.del(key);
                }
            }
            cursor = scanResult.getCursor();
        } while (!cursor.equals(ScanParams.SCAN_POINTER_START));
    }
}
