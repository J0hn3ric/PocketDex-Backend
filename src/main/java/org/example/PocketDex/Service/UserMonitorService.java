package org.example.PocketDex.Service;

import org.example.PocketDex.Utils.UserConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
        long threshold = now - (24L * 60 * 60);

        int totalDeleted = 0;

        // scan for every user in redis
        String cursor = ScanParams.SCAN_POINTER_START;
        ScanParams scanParams = new ScanParams().match("user_session:*").count(100);

        do {
            ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
            int deletedThisBatch = Flux.fromIterable(scanResult.getResult())
                    .flatMap(key -> {
                        String lastActiveStr = jedis.hget(key, UserConstants.LAST_ACTIVE_KEY);
                        if (lastActiveStr == null) { return Mono.empty(); }

                        long lastActive = Long.parseLong(lastActiveStr);
                        if (lastActive < threshold) {
                            String userId = key.replace("user_session:", "");

                            return userService.deleteUserUsingUserId(userId)
                                    .doOnSubscribe(s -> System.out.println("Deleting userId=" + userId))
                                    .then(Mono.fromRunnable(() -> {
                                        jedis.del(key);
                                        System.out.println("Deleted Redis key for userId=" + userId);
                                    }))
                                    .thenReturn(1);
                        }

                        return Mono.empty();
                    })
                    .reduce(0, Integer::sum)
                    .blockOptional()
                    .orElse(0);

            totalDeleted += deletedThisBatch;
            cursor = scanResult.getCursor();
        } while (!cursor.equals(ScanParams.SCAN_POINTER_START));

        System.out.println("Success: Inactive user check completed. Deleted " + totalDeleted + " users.");
    }
}
