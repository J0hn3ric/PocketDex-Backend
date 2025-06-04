package org.example.PocketDex.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.PocketDex.DTO.response.UpdateUserCardsResponse;
import org.example.PocketDex.Model.UserCard;
import org.example.PocketDex.Model.UserCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserCardService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    JWTService jwtService;

    public UserCardService(@Qualifier("supabaseWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<UpdateUserCardsResponse> updateUserCards(String jwtToken, List<UserCard> userCardsToBeUpdated) {
        try {
            String userToken = jwtService.extractUserToken(jwtToken);
            String userIdString = jwtService.getUserIdFromToken(userToken);

            UUID userId = UUID.fromString(userIdString);

            boolean allMatch = userCardsToBeUpdated.stream()
                    .allMatch(uc -> uc.getUserId().equals(userId));

            if (!allMatch) {
                throw new IllegalArgumentException("All UserCards must have the same UserId!");
            }

            UserCollection userCollection = new UserCollection(userId, userCardsToBeUpdated);

            List<UserCard> userCardsToUpsert = userCollection.getUserCardsToUpdate();
            List<UserCard> userCardsToDelete = userCollection.getUserCardsToRemove();

            Mono<JsonNode> upsertMono = userCardsToUpsert.isEmpty()
                    ? Mono.empty()
                    : upsertUserCards(userToken, userCardsToUpsert);

            Mono<JsonNode> deleteMono = userCardsToDelete.isEmpty()
                    ? Mono.empty()
                    : deleteUserCards(userToken, userCardsToDelete);

           return Mono.when(upsertMono, deleteMono)
                   .thenReturn(new UpdateUserCardsResponse(
                           "200",
                           "Updated UserCards table"
                   ));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return Mono.just(new UpdateUserCardsResponse(
                    "500",
                    "couldn't update UserCards table, caused by: " + e.getMessage()
            ));
        }
    }

    private Mono<JsonNode> deleteUserCards(String userKey, List<UserCard> userCardsToDelete) {
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
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userKey)
                .header("Prefer", "return=representation")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToMono(JsonNode.class);
    }

    private Mono<JsonNode> upsertUserCards(String userKey, List<UserCard> userCardsToUpdate) {
        return webClient
                .post()
                .uri("/UserCard")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userKey)
                .header("Prefer", "resolution=merge-duplicates, return=representation")
                .bodyValue(userCardsToUpdate)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToMono(JsonNode.class);
    }

    public Mono<List<UserCard>> getUserCardsByUserId(String jwtToken, String userId) {
        String userToken = jwtService.extractUserToken(jwtToken);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/UserCard")
                        .queryParam("user_id", "eq." + userId)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToFlux(UserCard.class)
                .collectList();
    }

    public Mono<List<UserCard>> getUserCardByCardId(String jwtToken, String cardId) {
        String userToken = jwtService.extractUserToken(jwtToken);
        String userId = jwtService.getUserIdFromToken(userToken);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/UserCard")
                        .queryParam("user_id", "eq." + userId)
                        .queryParam("card_id", "eq." + cardId)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToFlux(UserCard.class)
                .collectList();
    }
}
