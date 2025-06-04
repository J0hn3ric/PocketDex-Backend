package org.example.PocketDex.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.PocketDex.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${supabase.url}")
    private String authUrl;

    @Value("${supabase.secret.api.key}")
    private String authKey;

    @Autowired
    private JWTService jwtService;

    public UserService(@Qualifier("supabaseWebClient") WebClient webClient) { this.webClient = webClient; }

    public Mono<JsonNode> signup(String email, String password) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("email", email);
        payload.put("password", password);

        WebClient authWebClient = webClient.mutate()
                .baseUrl(authUrl + "/auth/v1")
                .build();

        return authWebClient
                .post()
                .uri("/signup")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authKey)
                .header("apikey", authKey)
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToMono(JsonNode.class);
    }

    public Mono<Void> insertUserProfileInfo(
            String friendId,
            String username,
            String userImg,
            String jwtToken
    ) {
        String userToken = jwtService.extractUserToken(jwtToken);
        String userId = jwtService.getUserIdFromToken(userToken);

        User user = new User(
                userId,
                friendId,
                username,
                userImg
        );


        return webClient
                .post()
                .uri("/User")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .bodyValue(user)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToMono(Void.class);
    }

    public Mono<List<User>> getUserProfileInfoById(String jwtToken, String userId) {
        String userToken = jwtService.extractUserToken(jwtToken);

        System.out.println(userToken);

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/User")
                        .queryParam("id", "eq." + userId)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToFlux(User.class)
                .collectList();
    }

    public Mono<List<User>> getUserProfileInfoByUsername(String jwtToken, String username) {
        String userToken = jwtService.extractUserToken(jwtToken);

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/User")
                        .queryParam("username", "eq." + username)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToFlux(User.class)
                .collectList();
    }

    public Mono<List<User>> updateUserProfile(String jwtToken, String newUsername, String newUserImg) {
        String userToken = jwtService.extractUserToken(jwtToken);
        String userId = jwtService.getUserIdFromToken(userToken);

        ObjectNode payload = objectMapper.createObjectNode();

        if (newUsername != null) {
          payload.put("username", newUsername);
        }

        if (newUserImg != null) {
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
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                    .header("Prefer", "return=representation")
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                    )
                    .bodyToFlux(User.class)
                    .collectList();
        } else {
            return Mono.error(new IllegalArgumentException("Illegal Argument: got empty request body"));
        }
    }

    public Mono<Void> deleteAuthUser(String jwtToken) {
        String userToken = jwtService.extractUserToken(jwtToken);
        String userIdFromToken = jwtService.getUserIdFromToken(userToken);

        WebClient authWebClient = webClient.mutate()
                .baseUrl(authUrl + "/auth/v1")
                .build();

        return authWebClient
                .delete()
                .uri("/admin/users/" + userIdFromToken)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authKey)
                .header("apikey", authKey)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToMono(Void.class);
    }

}
