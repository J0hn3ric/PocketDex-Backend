package org.example.PocketDex.Repository.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.PocketDex.DTO.response.UpdateUserCardsResponseDTO;
import org.example.PocketDex.DTO.response.UpdateUserProfileResponseDTO;
import org.example.PocketDex.Model.User;
import org.example.PocketDex.Utils.SupabaseConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository("supabaseUserRepository")
public class SupabaseUserRepository implements UserRepository {
    private final WebClient webClient;
    private final WebClient authWebClient;
    private final String authUrl;
    private final String authKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public SupabaseUserRepository(
            @Qualifier("supabaseWebClient") WebClient webClient,
            @Value("${supabase.url}") String authUrl,
            @Value("${supabase.secret.api.key}") String authKey
    ) {
        this.webClient = webClient;
        this.authUrl = authUrl;
        this.authKey = authKey;
        this.authWebClient = webClient.mutate()
                .baseUrl(this.authUrl + "/auth/v1")
                .build();
    }

    @Override
    public Mono<JsonNode> signup() {
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

    @Override
    public Mono<Void> createUser(User user, String accessToken) {
        return webClient
                .post()
                .uri("/User")
                .header(HttpHeaders.AUTHORIZATION, SupabaseConstants.TOKEN_PREFIX + accessToken)
                .bodyValue(user)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToMono(Void.class);
    }

    @Override
    public Mono<User> getUserById(String userId, String accessToken) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/User")
                        .queryParam("id", "eq." + userId)
                        .queryParam("select", "username,friend_id,user_img")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, SupabaseConstants.TOKEN_PREFIX + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToMono(User[].class)
                .flatMap(arr -> {
                    if (arr.length == 0) return Mono.error(new RuntimeException("User not found"));
                    return Mono.just(arr[0]);
                });
    }

    @Override
    public Mono<List<User>> getUsersByUsername(String username, String accessToken) {
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
                .collectList();
    }

    @Override
    public Mono<UpdateUserProfileResponseDTO> updateUser(String userId, ObjectNode updatedInfo, String accessToken) {
        return webClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/User")
                        .queryParam("id", "eq." + userId)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, SupabaseConstants.TOKEN_PREFIX + accessToken)
                .header("Prefer", "return=representation")
                .bodyValue(updatedInfo)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToMono(UpdateUserProfileResponseDTO[].class)
                .map(arr -> arr[0]);
    }

    @Override
    public Mono<Void> deleteUser(String userId) {
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
}
