package org.example.PocketDex.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.PocketDex.Utils.SessionContext;
import org.example.PocketDex.Utils.SupabaseConstants;
import org.example.PocketDex.Utils.UserConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

@Service
public class TokenService {

    private final WebClient authWebClient;
    private final SessionService sessionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String authUrl;
    private final String authKey;

    @Autowired
    public TokenService(
            @Qualifier("supabaseWebClient") WebClient webClient,
            SessionService sessionService,
            @Value("${supabase.url}") String authUrl,
            @Value("${supabase.secret.api.key}") String authKey
    ) {
        this.authUrl = authUrl;
        this.authKey = authKey;
        this.sessionService = sessionService;
        this.authWebClient = webClient.mutate()
                .baseUrl(this.authUrl + "/auth/v1")
                .build();

    }

    public <T> Mono<T> withValidSession(
            String backendToken,
            Function<SessionContext, Mono<T>> action
    ) {
        Map<String, String> sessionInfo = sessionService.getUserSessionInfo(backendToken);

        return validateAndReissueTokensIfRequired(sessionInfo)
                .flatMap(newSessionInfo -> {
                    String oldAccessTokenExp = sessionInfo.get(UserConstants.ACCESS_TOKEN_EXP_KEY);
                    String newBackendToken = sessionInfo.get(UserConstants.BACKEND_TOKEN_KEY);

                    checkAndUpdateTokens(
                            oldAccessTokenExp,
                            newSessionInfo,
                            newBackendToken
                    );

                    SessionContext context = new SessionContext(newSessionInfo);

                    return action.apply(context);
                });
    }

    private Mono<Map<String, String >> validateAndReissueTokensIfRequired(Map<String, String> sessionInfo) {
        Instant now = Instant.now();
        long expEpochSeconds = Long.parseLong(sessionInfo.get(UserConstants.ACCESS_TOKEN_EXP_KEY));
        Instant accessTokenExp = Instant.ofEpochSecond(expEpochSeconds);

        if (accessTokenExp.isBefore(now)) {
            return reissueTokens(sessionInfo);
        } else {
            return Mono.just(sessionInfo);
        }
    }

    private Mono<Map<String, String>> reissueTokens(Map<String, String> sessionInfo) {
        String refreshToken = sessionInfo.get(UserConstants.REFRESH_TOKEN_KEY);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put(UserConstants.REFRESH_TOKEN_KEY, refreshToken);

        return authWebClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/token")
                        .queryParam("grant_type", UserConstants.REFRESH_TOKEN_KEY)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, SupabaseConstants.TOKEN_PREFIX + authKey)
                .header(SupabaseConstants.API_KEY, authKey)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToMono(JsonNode.class)
                .map(responseBody ->  editSessionInfo(sessionInfo, responseBody));
    }

    private Map<String, String> editSessionInfo(
            Map<String, String> sessionInfo, JsonNode responseBody
    ) {
        sessionInfo.put(
                UserConstants.ACCESS_TOKEN_KEY,
                responseBody.get(UserConstants.ACCESS_TOKEN_KEY).asText()
        );
        sessionInfo.put(
                UserConstants.ACCESS_TOKEN_EXP_KEY,
                responseBody.get("expires_at").asText()
        );
        sessionInfo.put(
                UserConstants.REFRESH_TOKEN_KEY,
                responseBody.get(UserConstants.REFRESH_TOKEN_KEY).asText()
        );

        return sessionInfo;
    }

    private void checkAndUpdateTokens(
            String oldAccessExp,
            Map<String, String> newSessionInfo,
            String backendToken
    ) {
        boolean areTokensRefreshed = !oldAccessExp.equals(
                newSessionInfo.get(UserConstants.ACCESS_TOKEN_EXP_KEY)
        );

        if (areTokensRefreshed) {
            sessionService.updateAccessAndRefreshTokens(
                    backendToken,
                    newSessionInfo.get(UserConstants.ACCESS_TOKEN_KEY),
                    Long.parseLong(newSessionInfo.get(UserConstants.ACCESS_TOKEN_EXP_KEY)),
                    newSessionInfo.get(UserConstants.REFRESH_TOKEN_KEY)
            );
        }
    }

}
