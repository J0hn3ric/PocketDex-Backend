package org.example.PocketDex.Service.utils;

import org.example.PocketDex.Service.JWTService;
import org.example.PocketDex.Utils.SessionContext;
import org.example.PocketDex.Utils.UserConstants;

import java.util.UUID;

public final class SessionUtils {

    private SessionUtils() {}

    public static String getAccessToken(SessionContext sessionContext) {
        return sessionContext.sessionInfo().get(UserConstants.ACCESS_TOKEN_KEY);
    }

    public static String getBackendToken(SessionContext sessionContext) {
        return sessionContext.sessionInfo().get(UserConstants.BACKEND_TOKEN_KEY);
    }

    public static UUID getUserId(SessionContext sessionContext, JWTService jwtService) {
        String token = getAccessToken(sessionContext);
        return UUID.fromString(jwtService.getUserIdFromToken(token));
    }

    public static UUID getUserId(String accessToken, JWTService jwtService) {
        return UUID.fromString(jwtService.getUserIdFromToken(accessToken));
    }
}
