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
import org.example.PocketDex.Repository.user_card.UserCardRepository;
import org.example.PocketDex.Service.utils.SessionUtils;
import org.example.PocketDex.Utils.UserConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class UserCardService {

    private final UserCardRepository userCardRepository;
    private final JWTService jwtService;
    private final TokenService tokenService;
    private final CardService cardService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public UserCardService(
            @Qualifier("supabaseUserCardRepository") UserCardRepository userCardRepository,
            JWTService jwtService,
            TokenService tokenService,
            CardService cardService
    ) {
        this.userCardRepository = userCardRepository;
        this.jwtService = jwtService;
        this.tokenService = tokenService;
        this.cardService = cardService;
    }

    public Mono<ResponseBodyDTO<UpdateUserCardsResponseDTO>> addNewUserCards(
            String backendToken,
            List<UserCard> requestPayload
    ) {
        return tokenService.withValidSession(backendToken, sessionContext -> {
            String accessToken = SessionUtils.getAccessToken(sessionContext);
            String newBackendToken = SessionUtils.getBackendToken(sessionContext);
            UUID userId = SessionUtils.getUserId(accessToken, jwtService);

            requestPayload.forEach(userCard -> userCard.setUserId(userId));

            return userCardRepository.addUserCards(requestPayload, accessToken)
                    .map(userCards -> buildUpdateResponse(userCards, List.of(), newBackendToken));
        });
    }

    public Mono<ResponseBodyDTO<UpdateUserCardsResponseDTO>> updateUserCards(
            String backendToken,
            List<UserCard> requestPayload
    ) {
        return tokenService.withValidSession(backendToken, sessionContext -> {
            String accessToken = SessionUtils.getAccessToken(sessionContext);
            String newBackendToken = SessionUtils.getBackendToken(sessionContext);
            UUID userId = SessionUtils.getUserId(accessToken, jwtService);

            requestPayload.forEach(userCard -> userCard.setUserId(userId));

            UserCollection userCollection = new UserCollection(userId, requestPayload);

            List<UserCard> userCardsToUpsert = userCollection.getUserCardsToUpsert();
            List<UserCard> userCardsToDelete = userCollection.getUserCardsToRemove();

            Mono<JsonNode> upsertMono = userCardsToUpsert.isEmpty()
                    ? Mono.empty()
                    : userCardRepository.upsertUserCards(userCardsToUpsert, accessToken);

            Mono<JsonNode> deleteMono = userCardsToDelete.isEmpty()
                    ? Mono.empty()
                    : userCardRepository.deleteUserCards(userCardsToDelete, accessToken);

            return Mono
                    .when(upsertMono, deleteMono)
                    .thenReturn(buildUpdateResponse(
                            userCardsToUpsert,
                            userCardsToDelete,
                            newBackendToken
                    ));
        });
    }

    public Mono<ResponseBodyDTO<List<UserCardWithCardInfoResponseDTO>>> getOwnedUserCards(
            String backendToken
    ) {
        return tokenService.withValidSession(backendToken, sessionContext -> {
            String accessToken = SessionUtils.getAccessToken(sessionContext);
            String newBackendToken = SessionUtils.getBackendToken(sessionContext);
            UUID userId = SessionUtils.getUserId(accessToken, jwtService);

            return userCardRepository.getUserCardsByUserId(userId.toString(), accessToken, cardService)
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
            String accessToken = SessionUtils.getAccessToken(sessionContext);
            String newBackendToken = SessionUtils.getBackendToken(sessionContext);

            return userCardRepository.getUserCardsByUserId(userId, accessToken, cardService)
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
            String accessToken = SessionUtils.getAccessToken(sessionContext);
            String newBackendToken = SessionUtils.getBackendToken(sessionContext);
            UUID userId = SessionUtils.getUserId(accessToken, jwtService);

            return userCardRepository.getUserCardByCardId(userId.toString(), cardId, accessToken)
                    .map(userCards -> new ResponseBodyDTO<>(userCards, newBackendToken));
        });
    }


    private ResponseBodyDTO<UpdateUserCardsResponseDTO> buildUpdateResponse(
            List<UserCard> upsertedUserCards,
            List<UserCard> deletedUserCards,
            String backendToken
    ) {
        List<String> upsertedIds = upsertedUserCards.stream()
                .map(UserCard::getCardId)
                .toList();

        List<String> deletedIds = deletedUserCards.stream()
                .map(UserCard::getCardId)
                .toList();

        return new ResponseBodyDTO<>(
                new UpdateUserCardsResponseDTO(upsertedIds, deletedIds),
                backendToken
        );
    }
}
