package org.example.PocketDex.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.PocketDex.DTO.response.ResponseBodyDTO;
import org.example.PocketDex.DTO.response.UpdateUserProfileResponseDTO;
import org.example.PocketDex.Model.User;
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

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final WebClient webClient;
    private final WebClient authWebClient;
    private final JWTService jwtService;
    private final SessionService sessionService;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String authUrl;
    private final String authKey;

    @Autowired
    public UserService(
            @Qualifier("supabaseWebClient") WebClient webClient,
            JWTService jwtService,
            SessionService sessionService,
            TokenService tokenService,
            @Value("${supabase.url}") String authUrl,
            @Value("${supabase.secret.api.key}") String authKey

    ) {
        this.webClient = webClient;
        this.jwtService = jwtService;
        this.tokenService = tokenService;
        this.sessionService = sessionService;
        this.authUrl = authUrl;
        this.authKey = authKey;
        this.authWebClient = webClient.mutate()
                .baseUrl(this.authUrl + "/auth/v1")
                .build();

    }

    public Mono<ResponseBodyDTO<Void>> createNewUser(
            String friendId,
            String username,
            String userImg
    ) {
        return anonymousSignup().flatMap(anonymousResponse -> {
            String accessToken = anonymousResponse.get(UserConstants.ACCESS_TOKEN_KEY).asText();
            long accessTokenExpiration = anonymousResponse.get("expires_at").asLong();
            String refreshToken = anonymousResponse.get(UserConstants.REFRESH_TOKEN_KEY). asText();
            String userId = anonymousResponse.get("user").get("id").asText();

            String backendToken = sessionService.createSession(
                    accessToken, accessTokenExpiration, refreshToken
            );

            User user = new User(
                    userId,
                    friendId,
                    username,
                    userImg
            );


            return webClient
                    .post()
                    .uri("/User")
                    .header(HttpHeaders.AUTHORIZATION, SupabaseConstants.TOKEN_PREFIX + accessToken)
                    .bodyValue(user)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                    )
                    .bodyToMono(Void.class)
                    .then(Mono.just(new ResponseBodyDTO<>(null, backendToken)));
        });
    }

    public Mono<ResponseBodyDTO<List<User>>> getUserInfoById(String backendToken, String userId) {
        return tokenService.withValidSession(backendToken, sessionContext -> {
            String accessToken = sessionContext
                    .sessionInfo()
                    .get(UserConstants.ACCESS_TOKEN_KEY);

            String newBackendToken = sessionContext
                    .sessionInfo()
                    .get(UserConstants.BACKEND_TOKEN_KEY);

            return webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/User")
                            .queryParam("id", "eq." + userId)
                            .build())
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            SupabaseConstants.TOKEN_PREFIX + accessToken
                    )
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                    )
                    .bodyToFlux(User.class)
                    .collectList()
                    .map(users -> new ResponseBodyDTO<>(users, newBackendToken));
        });
    }

    public Mono<ResponseBodyDTO<List<User>>> getUserInfoByUsername(String backendToken, String username) {
        return tokenService.withValidSession(backendToken, sessionContext -> {
            String accessToken = sessionContext
                    .sessionInfo()
                    .get(UserConstants.ACCESS_TOKEN_KEY);

            String newBackendToken = sessionContext
                    .sessionInfo()
                    .get(UserConstants.BACKEND_TOKEN_KEY);

            return webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/User")
                            .queryParam("username", "eq." + username)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, SupabaseConstants.TOKEN_PREFIX + accessToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                    )
                    .bodyToFlux(User.class)
                    .collectList()
                    .map(users -> new ResponseBodyDTO<>(users, newBackendToken));
        });
    }

    public Mono<ResponseBodyDTO<List<UpdateUserProfileResponseDTO>>> updateUserInfo(
            String backendToken, String newUsername, String newUserImg
    ) {
        return tokenService.withValidSession(backendToken, sessionContext -> {
            String accessToken = sessionContext
                    .sessionInfo()
                    .get(UserConstants.ACCESS_TOKEN_KEY);

            String newBackendToken = sessionContext
                    .sessionInfo()
                    .get(UserConstants.BACKEND_TOKEN_KEY);

            String userId = jwtService.getUserIdFromToken(accessToken);

            ObjectNode payload = objectMapper.createObjectNode();

            if (newUsername != null && !newUsername.isEmpty()) {
                payload.put("username", newUsername);
            }

            if (newUserImg != null && !newUserImg.isEmpty()) {
                payload.put("user_img", newUserImg);
            }

            System.out.println(payload.toString());

            if (!payload.isEmpty()) {
                return webClient
                        .patch()
                        .uri(uriBuilder -> uriBuilder
                                .path("/User")
                                .queryParam("id", "eq." + userId)
                                .build())
                        .header(HttpHeaders.AUTHORIZATION, SupabaseConstants.TOKEN_PREFIX + accessToken)
                        .header("Prefer", "return=representation")
                        .bodyValue(payload)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, response ->
                                response.bodyToMono(String.class)
                                        .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                        )
                        .bodyToFlux(UpdateUserProfileResponseDTO.class)
                        .collectList()
                        .map(users -> new ResponseBodyDTO<>(users, newBackendToken));
            } else {
                throw new IllegalArgumentException("Illegal Argument: got empty request body");
            }
        });
    }

    public Mono<ResponseBodyDTO<String>> deleteUserUsingBackendToken(String backendToken) {
        Map<String, String> sessionInfo = sessionService.getUserSessionInfo(backendToken);
        String accessToken = sessionInfo.get(UserConstants.ACCESS_TOKEN_KEY);

        String userIdFromToken = jwtService.getUserIdFromToken(accessToken);

        sessionService.deleteSession(backendToken);

        return authWebClient
                .delete()
                .uri("/admin/users/" + userIdFromToken)
                .header(HttpHeaders.AUTHORIZATION, SupabaseConstants.TOKEN_PREFIX + authKey)
                .header(SupabaseConstants.API_KEY, authKey)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToMono(String.class)
                .then(Mono.just(new ResponseBodyDTO<>("user deleted successfully!", null)));
    }

    public Mono<Void> deleteUserUsingUserId(String userId) {
        return authWebClient
                .delete()
                .uri("/admin/users/" + userId)
                .header(HttpHeaders.AUTHORIZATION, SupabaseConstants.TOKEN_PREFIX + authKey)
                .header(SupabaseConstants.API_KEY, authKey)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToMono(Void.class);
    }

    private Mono<JsonNode> anonymousSignup() {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("anonymous", true);

        return authWebClient
                .post()
                .uri("/signup")
                .header(HttpHeaders.AUTHORIZATION, SupabaseConstants.TOKEN_PREFIX + authKey)
                .header(SupabaseConstants.API_KEY, authKey)
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToMono(JsonNode.class);
    }



}
