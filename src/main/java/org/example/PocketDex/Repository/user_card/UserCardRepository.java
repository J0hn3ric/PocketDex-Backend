package org.example.PocketDex.Repository.user_card;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.PocketDex.DTO.response.UpdateUserCardsResponseDTO;
import org.example.PocketDex.DTO.response.UserCardWithCardInfoResponseDTO;
import org.example.PocketDex.Model.UserCard;
import org.example.PocketDex.Service.CardService;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserCardRepository {
    Mono<List<UserCardWithCardInfoResponseDTO>> getUserCardsByUserId(
            String userId, String accessToken, CardService cardService
    );
    Mono<List<UserCard>> getUserCardByCardId(String userId, String cardId, String accessToken);
    Mono<List<UserCard>> addUserCards(List<UserCard> userCards, String accesToken);
    Mono<JsonNode> deleteUserCards(List<UserCard> userCards, String accessToken);
    Mono<JsonNode> upsertUserCards(List<UserCard> userCards, String accessToken);
}
