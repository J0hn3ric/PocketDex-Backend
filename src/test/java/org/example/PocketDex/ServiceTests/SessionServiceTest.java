package org.example.PocketDex.ServiceTests;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.JwtException;
import org.example.PocketDex.Service.SessionService;
import org.example.PocketDex.Utils.UserConstants;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SessionServiceTest {
    @Autowired
    private SessionService sessionService;

    @BeforeAll
    static void loadEnv() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
    }

    long now = Instant.now().getEpochSecond();
    final String testAccessToken = "test_access_token";
    final String testRefreshToken = "test_refresh_token";
    final long testAccessExp = now + 600;

    String backendToken;

    @AfterEach
    void deleteSession() {
        if (backendToken != null) {
            sessionService.deleteSession(backendToken);

            System.out.println("deleted test session");
        } else {
            System.out.println("no backend token!!!");
        }
    }

    @Test
    void sessionService_CreateUserSession_UserSessionIsCreatedCorrectly() {
        try {
            backendToken = sessionService.createSession(
                    testAccessToken,
                    testAccessExp, testRefreshToken
            );

            assertNotNull(backendToken);


            Map<String, String> result = sessionService.getUserSessionInfo(backendToken);

            assertAll(
                    () -> assertEquals(backendToken, result.get("backend_token")),
                    () -> assertEquals(testAccessToken, result.get("access_token")),
                    () -> assertEquals(String.valueOf(testAccessExp), result.get("access_expires_at")),
                    () -> assertEquals(testRefreshToken, result.get("refresh_token")),
                    () -> assertEquals(
                            String.valueOf(Instant.now().getEpochSecond()), result.get("last_active")
                    )
            );
        } catch (Exception e) {
            deleteSession();
            System.out.println("Error: " + e.getMessage());
            assertFalse(true);
        }
    }

    @Test
    void sessionService_GetSessionInfoWithInvalidToken_ThrowsJWTException() {
        String invalidToken = "invalid";

        assertThrows(
                RuntimeException.class,
                () ->  sessionService.getUserSessionInfo(invalidToken)
        );
    }

    @Test
    void sessionService_GetSessionInfoOfDeletedSession_ThrowsRuntimeException() {
        assertThrows(
                RuntimeException.class,
                () -> sessionService.getUserSessionInfo(backendToken)
        );
    }

    @Nested
    public class SessionAlreadyPresentInRedis {

        @BeforeEach
        void addSessionToRedis() {
            backendToken = sessionService.createSession(
                    testAccessToken,
                    testAccessExp, testRefreshToken
            );
        }

        @Test
        void sessionService_GetSessionInfoSessionPresentInRedis_SessionIsReturnedCorrectly() {
            String expectedAccessToken = testAccessToken;
            String expectedRefreshToken = testRefreshToken;
            String expectedAccessExp = String.valueOf(testAccessExp);



            try {
                Map<String, String> response = sessionService.getUserSessionInfo(backendToken);

                assertAll(
                        () -> assertEquals(backendToken, response.get("backend_token")),
                        () -> assertEquals(expectedAccessToken, response.get("access_token")),
                        () -> assertEquals(expectedAccessExp, response.get("access_expires_at")),
                        () -> assertEquals(expectedRefreshToken, response.get("refresh_token")),
                        () -> assertEquals(
                                String.valueOf(Instant.now().getEpochSecond()), response.get("last_active")
                        )
                );

            } catch (Exception e) {
                deleteSession();
                System.out.println("Error: " + e.getMessage());
                assertFalse(true);
            }
        }

        @Test
        void sessionService_UpdateAccessTokenWithSessionPresentInRedis_SessionIsUpdatedCorrectly() {
            String updatedAccessToken = "updated";
            long updatedAccessExp = Instant.now().getEpochSecond() + 600;
            String updatedRefreshToken = "newRefresh";

            try {
                sessionService.updateAccessAndRefreshTokens(
                        backendToken,
                        updatedAccessToken,
                        updatedAccessExp,
                        updatedRefreshToken
                );

                Map<String, String> response = sessionService.getUserSessionInfo(backendToken);
                String newBackendToken = response.get(UserConstants.BACKEND_TOKEN_KEY);


                assertAll(
                        () -> assertEquals(backendToken, newBackendToken),
                        () -> assertEquals(updatedAccessToken, response.get("access_token")),
                        () -> assertEquals(String.valueOf(updatedAccessExp), response.get("access_expires_at")),
                        () -> assertEquals(updatedRefreshToken, response.get("refresh_token")),
                        () -> assertEquals(
                                String.valueOf(Instant.now().getEpochSecond()), response.get("last_active")
                        )
                );
            } catch (Exception e) {
                deleteSession();
                System.out.println("Error: " + e.getMessage());
                assertFalse(true);
            }
        }

        @Test
        void sessionService_UpdateAccessTokenWithSessionNotPresentInRedis_ThrowsRuntimeException() {
            sessionService.deleteSession(backendToken);

            assertThrows(
                    RuntimeException.class,
                    () -> sessionService.updateAccessAndRefreshTokens(
                            backendToken,
                            "random",
                            Instant.now().getEpochSecond() + 600,
                            "newRefresh"
                    )
            );
        }


    }

}
