package org.example.PocketDex.Repository.user_card;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.PocketDex.DTO.response.UpdateUserCardsResponseDTO;
import org.example.PocketDex.DTO.response.UserCardWithCardInfoResponseDTO;
import org.example.PocketDex.Model.UserCard;
import org.example.PocketDex.Service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository("supabaseUserCardRepository")
public class SupabaseUserCardRepository implements UserCardRepository {
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public SupabaseUserCardRepository(
            @Qualifier("supabaseWebClient") WebClient webClient
    ) {
        this.webClient = webClient;
    }

    @Override
    public Mono<List<UserCardWithCardInfoResponseDTO>> getUserCardsByUserId(
            String userId, String accessToken, CardService cardService
    ) {
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
                .collectList();
    }

    @Override
    public Mono<List<UserCard>> getUserCardByCardId(String userId, String cardId, String accessToken) {
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
                .collectList();
    }

    @Override
    public Mono<List<UserCard>> addUserCards(List<UserCard> userCards, String accessToken) {
        return webClient.post()
                .uri("/rpc/bulk_upsert_user_cards")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .bodyValue(createBulkUpsertRequestBody(userCards))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToFlux(UserCard.class)
                .collectList();
    }

    @Override
    public Mono<JsonNode> upsertUserCards(List<UserCard> userCards, String accessToken) {
        return webClient
                .post()
                .uri("/UserCard")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header("Prefer", "resolution=merge-duplicates, return=representation")
                .bodyValue(userCards)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToMono(JsonNode.class);
    }

    @Override
    public Mono<JsonNode> deleteUserCards(List<UserCard> userCards, String accessToken) {
        return webClient.post()
                .uri("/rpc/bulk_delete_by_user_card")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header("Prefer", "return=representation")
                .bodyValue(createBulkDeleteRequestBody(userCards))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Failed: " + body)))
                )
                .bodyToMono(JsonNode.class);
    }

    private ObjectNode createBulkUpsertRequestBody(List<UserCard> userCards) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("table_name", "UserCard");
        requestBody.put("user_column", "user_id");
        requestBody.put("card_column", "card_id");
        requestBody.put("amount_column", "quantity");

        ArrayNode payload = objectMapper.createArrayNode();
        for (UserCard uc : userCards) {
            ObjectNode userCard = objectMapper.createObjectNode();
            userCard.put("user_id", uc.getUserId().toString());
            userCard.put("card_id", uc.getCardId());
            userCard.put("quantity", uc.getQuantity());
            payload.add(userCard);
        }

        requestBody.set("payload", payload);

        return requestBody;
    }

    private ObjectNode createBulkDeleteRequestBody(List<UserCard> userCards) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("table_name", "UserCard");
        requestBody.put("user_column", "user_id");
        requestBody.put("card_column", "card_id");

        ArrayNode pairs = objectMapper.createArrayNode();
        for (UserCard uc : userCards) {
            ObjectNode pair = objectMapper.createObjectNode();
            pair.put("user_id", uc.getUserId().toString());
            pair.put("card_id", uc.getCardId());
            pairs.add(pair);
        }

        requestBody.set("pairs", pairs);
        return requestBody;
    }
}
