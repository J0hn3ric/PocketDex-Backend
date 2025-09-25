package org.example.PocketDex.Service;

import io.jsonwebtoken.Claims;
import jakarta.annotation.PreDestroy;
import org.apache.catalina.User;
import org.example.PocketDex.Exceptions.SessionExpiredException;
import org.example.PocketDex.Utils.UserConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.UnifiedJedis;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class SessionService {

    private final UnifiedJedis jedis;
    private final JWTService jwtService;
    private final String prefixKey = "user_session:";

    @Autowired
    public SessionService(UnifiedJedis jedis, JWTService jwtService) {
        this.jedis = jedis;
        this.jwtService = jwtService;
    }

    @PreDestroy
    public void cleanup() {
        jedis.close();
    }



    /*
    * !!!ATTENTION!!!
    * Refresh token has no set TTL, but is one-time use only!!
    * that means that we have to check for the last time the user has requested a refresh token
    * if the user doesn't request a refresh token for a month -> silently delete his data
     */

    // create a new Session, userId is UUID made to String and created by the caller
    public String createSession(
            String accessToken,
            long accessExp,
            String refreshToken
    ) {
        String sessionId = UUID.randomUUID().toString();

        jedis.hset(prefixKey + sessionId, Map.of(
                UserConstants.ACCESS_TOKEN_KEY, accessToken,
                UserConstants.ACCESS_TOKEN_EXP_KEY, String.valueOf(accessExp),
                UserConstants.REFRESH_TOKEN_KEY, refreshToken,
                UserConstants.LAST_ACTIVE_KEY, String.valueOf(Instant.now().getEpochSecond())
        ));

        return jwtService.generateTokenFromSessionId(sessionId);
    }



    public Map<String, String> getUserSessionInfo(String backendToken) {
        Map<String, String> tokenPayload = getSessionIdAndExpirationFromBackendToken(backendToken);
        String sessionId = tokenPayload.get(UserConstants.SESSION_ID_KEY);
        Instant exp = Instant.parse(tokenPayload.get(UserConstants.BACKEND_TOKEN_EXP_KEY));

        String key = prefixKey + sessionId;

        Map<String, String> userSession = jedis.hgetAll(key);

        if (userSession == null || userSession.isEmpty()) {
            // refresh token is expired, session doesn't exist in redis anymore
            // once this is flagged, should remove the auth.user table tied to
            throw new SessionExpiredException();
        }

        // reissue backend token if it becomes expired -> 15 mins TTL of backend token
        backendToken = validateAndReissueTokenIfExpired(
                exp,
                sessionId,
                backendToken
        );

        userSession.put(UserConstants.BACKEND_TOKEN_KEY, backendToken);

        return userSession;
    }



    /*
    * When requesting for new tokens with refresh token,
    * Supabase generates and returns a new access_token with it's expiration
    * AND a new refresh_token, that is always one time use only.
    * To check if the user has maybe uninstalled the app or not using it anymore
    * we can check the last time the user has requested a new refresh_token (last_active)
    * and if the user has been inactive for 30 days, we cleanup the user's data
     */
    public void updateAccessAndRefreshTokens(
            String backendToken, String newAccessToken,
            long newAccessExp, String newRefreshToken
    ) {
        Map<String, String> tokenPayload = getSessionIdAndExpirationFromBackendToken(backendToken);
        String sessionId = tokenPayload.get(UserConstants.SESSION_ID_KEY);
        Instant exp = Instant.parse(tokenPayload.get(UserConstants.BACKEND_TOKEN_EXP_KEY));

        String key = prefixKey + sessionId;
        if (!jedis.exists(key)) {
            throw new SessionExpiredException();
        }

        jedis.hset(key, Map.of(
                UserConstants.ACCESS_TOKEN_KEY, newAccessToken,
                UserConstants.ACCESS_TOKEN_EXP_KEY, String.valueOf(newAccessExp),
                UserConstants.REFRESH_TOKEN_KEY, newRefreshToken,
                UserConstants.LAST_ACTIVE_KEY, String.valueOf(Instant.now().getEpochSecond())
        ));
    }

    public void deleteSession(String backendToken) {
        String userId = getSessionIdAndExpirationFromBackendToken(backendToken)
                .get(UserConstants.SESSION_ID_KEY);

        jedis.del(prefixKey + userId);
    }

    private String validateAndReissueTokenIfExpired(
            Instant exp, String userId, String oldToken
    ) {
        Instant now = Instant.now();
        String newToken = null;

        if (exp.isBefore(now)) {
            newToken = jwtService.generateTokenFromSessionId(userId);
        }

        return newToken != null ? newToken : oldToken;
    }

    private Map<String, String> getSessionIdAndExpirationFromBackendToken(String backendToken) {
        Claims tokenPayload = jwtService.getPayloadFromToken(backendToken);

        return Map.of(
                UserConstants.SESSION_ID_KEY, tokenPayload.getSubject(),
                UserConstants.BACKEND_TOKEN_EXP_KEY, tokenPayload.getExpiration()
                        .toInstant()
                        .toString()
        );
    }


}
