package org.example.PocketDex.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.PocketDex.DTO.response.ResponseBodyDTO;
import org.example.PocketDex.DTO.response.UpdateUserCardsResponseDTO;
import org.example.PocketDex.DTO.response.UserCardWithCardInfoResponseDTO;
import org.example.PocketDex.Model.UserCard;
import org.example.PocketDex.Model.UserCollection;
import org.example.PocketDex.Utils.UserConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class UserCardService {

    private final WebClient webClient;
    private final JWTService jwtService;
    private final TokenService tokenService;
    private final CardService cardService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public UserCardService(
            @Qualifier("supabaseWebClient") WebClient webClient,
            JWTService jwtService,
            TokenService tokenService,
            CardService cardService
    ) {
        this.webClient = webClient;
        this.jwtService = jwtService;
        this.tokenService = tokenService;
        this.cardService = cardService;
    }

    public Mono<ResponseBodyDTO<UpdateUserCardsResponseDTO>> updateUserCards(
            String backendToken,
            List<UserCard> requestPayload
    ) {
        return tokenService.withValidSession(backendToken, sessionContext -> {
            String accessToken = sessionContext
                    .sessionInfo()
                    .get(UserConstants.ACCESS_TOKEN_KEY);

            String newBackendToken = sessionContext
                    .sessionInfo()
                    .get(UserConstants.BACKEND_TOKEN_KEY);

            String userIdString = jwtService.getUserIdFromToken(accessToken);

            UUID userId = UUID.fromString(userIdString);

            requestPayload.forEach(userCard -> userCard.setUserId(userId));

            UserCollection userCollection = new UserCollection(userId, requestPayload);

            List<UserCard> userCardsToUpsert = userCollection.getUserCardsToUpsert();
            List<UserCard> userCardsToDelete = userCollection.getUserCardsToRemove();

            Mono<JsonNode> upsertMono = userCardsToUpsert.isEmpty()
                    ? Mono.empty()
                    : upsertUserCards(accessToken, userCardsToUpsert);

            Mono<JsonNode> deleteMono = userCardsToDelete.isEmpty()
                    ? Mono.empty()
                    : deleteUserCards(accessToken, userCardsToDelete);

            return Mono
                    .when(upsertMono, deleteMono)
                    .thenReturn(new ResponseBodyDTO<>(
                            new UpdateUserCardsResponseDTO(
                                    userCardsToUpsert
                                            .stream()
                                            .map(UserCard::getCardId)
                                            .toList(),
                                    userCardsToDelete
                                            .stream()
                                            .map(UserCard::getCardId)
                                            .toList()
                            ),
                            newBackendToken
                    ));
        });
    }

    public Mono<ResponseBodyDTO<List<UserCardWithCardInfoResponseDTO>>> getOwnedUserCards(
            String backendToken
    ) {
        return tokenService.withValidSession(backendToken, sessionContext -> {
            String accessToken = sessionContext
                    .sessionInfo()
                    .get(UserConstants.ACCESS_TOKEN_KEY);

            String newBackendToken = sessionContext
                    .sessionInfo()
                    .get(UserConstants.BACKEND_TOKEN_KEY);

            String userId = jwtService.getUserIdFromToken(accessToken);

            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/UserCard")
                            .queryParam("user_id", "eq." + userId)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                    )
                    .bodyToFlux(UserCard.class)
                    .collectList()
                    .flatMapMany(cardService::getUserCardsWithInfo)
                    .collectList()
                    .map(userCardsWithInfo -> new ResponseBodyDTO<>(
                            userCardsWithInfo,
                            newBackendToken
                    ));
        });
    }

    public Mono<ResponseBodyDTO<List<UserCardWithCardInfoResponseDTO>>> getUserCardsByUserId(
            String backendToken,
            String userId
    ) {
        return  tokenService.withValidSession(backendToken, sessionContext -> {
            String accessToken = sessionContext
                    .sessionInfo()
                    .get(UserConstants.ACCESS_TOKEN_KEY);

            String newBackendToken = sessionContext
                    .sessionInfo()
                    .get(UserConstants.BACKEND_TOKEN_KEY);

            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/UserCard")
                            .queryParam("user_id", "eq." + userId)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                    )
                    .bodyToFlux(UserCard.class)
                    .collectList()
                    .flatMapMany(cardService::getUserCardsWithInfo)
                    .collectList()
                    .map(userCardsWithInfo -> new ResponseBodyDTO<>(
                            userCardsWithInfo,
                            newBackendToken
                    ));
        });
    }

    public Mono<ResponseBodyDTO<List<UserCard>>> getUserCardByCardId(
            String backendToken,
            String cardId
    ) {
        return tokenService.withValidSession(backendToken, sessionContext -> {
            String accessToken = sessionContext
                    .sessionInfo()
                    .get(UserConstants.ACCESS_TOKEN_KEY);

            String newBackendToken = sessionContext
                    .sessionInfo()
                    .get(UserConstants.BACKEND_TOKEN_KEY);

            String userId = jwtService.getUserIdFromToken(accessToken);

            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/UserCard")
                            .queryParam("user_id", "eq." + userId)
                            .queryParam("card_id", "eq." + cardId)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                    )
                    .bodyToFlux(UserCard.class)
                    .collectList()
                    .map(userCards -> new ResponseBodyDTO<>(userCards, newBackendToken));
        });
    }

    private Mono<JsonNode> deleteUserCards(
            String accessToken,
            List<UserCard> userCardsToDelete
    ) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("table_name", "UserCard");
        requestBody.put("user_column", "user_id");
        requestBody.put("card_column", "card_id");

        ArrayNode pairs = objectMapper.createArrayNode();
        for (UserCard uc : userCardsToDelete) {
            ObjectNode pair = objectMapper.createObjectNode();
            pair.put("user_id", uc.getUserId().toString());
            pair.put("card_id", uc.getCardId());
            pairs.add(pair);
        }

        requestBody.set("pairs", pairs);

        return webClient.post()
                .uri("/rpc/bulk_delete_by_user_card")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header("Prefer", "return=representation")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToMono(JsonNode.class);
    }

    private Mono<JsonNode> upsertUserCards(
            String accessToken,
            List<UserCard> userCardsToUpdate
    ) {
        return webClient
                .post()
                .uri("/UserCard")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header("Prefer", "resolution=merge-duplicates, return=representation")
                .bodyValue(userCardsToUpdate)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToMono(JsonNode.class);
    }
}
