package org.example.PocketDex.ServiceTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.github.cdimascio.dotenv.Dotenv;
import org.example.PocketDex.DTO.response.UpdateUserCardsResponse;
import org.example.PocketDex.Model.UserCard;
import org.example.PocketDex.Model.UserCollection;
import org.example.PocketDex.Service.UserCardService;
import org.example.PocketDex.Service.UserService;
import org.example.PocketDex.ServiceTests.helpers.UserServiceHelper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
public class UserCardServiceTest {

    @Autowired
    private UserCardService userCardService;

    @Autowired
    private UserService userService;

    private ObjectMapper objectMapper = new ObjectMapper();

    final String testEmail = "test-email@example.com";
    final String testPassword = "test-password";

    @BeforeAll
    static void loadEnv() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
    }

    private String userId;
    private String userKey;

    @BeforeEach
    void configureUser() {
        JsonNode response = userService.signup(
                testEmail,
                testPassword
        ).block();

        userId = response.get("user").get("id").asText();
        userKey = "Bearer " + response.get("access_token").asText();

        UserServiceHelper.insertNewUserProfileInformation(
                userKey,
                userService
        );
    }

    @AfterEach
    void deleteTestUser() {
        String url = System.getProperty("SUPABASE_URL");
        String apiKey = System.getProperty("SUPABASE_SECRET_API_KEY");

        WebClient
                .builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader("apikey", apiKey)
                .build()
                .delete()
                .uri("/auth/v1/admin/users/" + userId)
                .retrieve()
                .bodyToMono(Void.class)
                .block();

        System.out.println("deleted test users!!");
    }

    @Test
    void userCardService_GetUserCardsWhenTableIsEmpty_ReturnsEmptyArray() {
        try {
            List<UserCard> response = userCardService
                    .getUserCardsByUserId(userKey, userId)
                    .block();

            assertEquals(0, response.size());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            deleteTestUser();
            boolean failed = true;
            assertFalse(failed);
        }
    }

    @Test
    void userCardService_AddOneUserCardToDB_CardAddedToDb() {

        int quantityExpected = 2;
        String cardIdExpected = "card1";

        String responseStatusExpected = "200";
        String responseMessageExpected = "Updated UserCards table";

        List<UserCard> expectedResponse = List.of(new UserCard(
                cardIdExpected,
                UUID.fromString(userId),
                quantityExpected
        ));

        try {

            UpdateUserCardsResponse response = userCardService.updateUserCards(
                    userKey,
                    expectedResponse
            ).block();

            List<UserCard> getResponse = userCardService.getUserCardsByUserId(userKey, userId)
                    .block();

            assertAll(
                    () -> assertEquals(cardIdExpected, getResponse.get(0).getCardId()),
                    () -> assertEquals(quantityExpected, getResponse.get(0).getQuantity()),
                    () -> assertEquals(userId, getResponse.get(0).getUserId().toString())
            );

            assertAll(
                    () -> assertEquals(responseStatusExpected, response.getStatus()),
                    () -> assertEquals(responseMessageExpected, response.getMessage())
            );
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            deleteTestUser();
            boolean failed = true;
            assertFalse(failed);
        }
    }

    @Nested
    public class UserCardsAlreadyAddedToDb {

        @BeforeEach
        void addManyUserCards() {
            UUID userIdToSend = UUID.fromString(userId);

            List<UserCard> userCardsToUpdate = List.of(
                    new UserCard("card1", userIdToSend, 3),
                    new UserCard("card2", userIdToSend, 5),
                    new UserCard("card3", userIdToSend, 7),
                    new UserCard("card4", userIdToSend, 38),
                    new UserCard("card5", userIdToSend, 1)
            );
            userCardService.updateUserCards(userKey, userCardsToUpdate).block();
        }

        @Test
        void userCardService_DeleteSomeUserCards_ReturnsCorrectStatusCodeAndMessage() {
            UUID userIdToSend = UUID.fromString(userId);

            List<UserCard> userCardsToDelete = List.of(
                    new UserCard("card1", userIdToSend, 0),
                    new UserCard("card5", userIdToSend, -2)
            );

            String expectedResponseStatus = "200";
            String expectedResponseMessage = "Updated UserCards table";

            try {
                UpdateUserCardsResponse response = userCardService
                        .updateUserCards(userKey, userCardsToDelete)
                        .block();

                assertAll(
                        () -> assertEquals(expectedResponseStatus, response.getStatus()),
                        () -> assertEquals(expectedResponseMessage, response.getMessage())
                );

                List<UserCard> getResponse = userCardService.getUserCardsByUserId(userKey, userId).block();

                assertEquals(3, getResponse.size());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                deleteTestUser();
                boolean failed = true;
                assertFalse(failed);
            }
        }

        @Test
        void userCardService_GetUserCardWithValidId_UserCardReturned() {
            String userCardIdToLookFor = "card1";
            UserCard expectedUserCard = new UserCard(
                    userCardIdToLookFor,
                    UUID.fromString(userId),
                    3
            );

            try {
                List<UserCard> response = userCardService.getUserCardByCardId(
                        userKey,
                        userCardIdToLookFor
                ).block();

                response.forEach(uc -> {
                    String userCardIdReturned = uc.getCardId();
                    String userIdReturned = uc.getUserId().toString();
                    int quantityReturned = uc.getQuantity();

                    assertAll(
                            () -> assertEquals(userCardIdReturned, expectedUserCard.getCardId()),
                            () -> assertEquals(userIdReturned, expectedUserCard.getUserId().toString()),
                            () -> assertEquals(quantityReturned, expectedUserCard.getQuantity())
                    );
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                deleteTestUser();
                boolean failed = true;
                assertFalse(failed);
            }
        }

        @Test
        void userCardService_UpsertSomeUserCards_ReturnsCorrectStatusCodeAndMessage() {
            UUID userIdToSend = UUID.fromString(userId);

            List<UserCard> userCardsToUpsert = List.of(
                    new UserCard("card1", userIdToSend, 1),
                    new UserCard("card2", userIdToSend, 10),
                    new UserCard("card11", userIdToSend, 1),
                    new UserCard("card20", userIdToSend, 1)
            );

            int expectedNumberOfUserCards = 7;
            UserCard expectedUserCardUpdated1 = new UserCard(
                    "card1",
                    UUID.fromString(userId),
                    1
            );

            UserCard expectedUserCardUpdated2 = new UserCard(
                    "card2",
                    UUID.fromString(userId),
                    10
            );

            String expectedResponseStatus = "200";
            String expectedResponseMessage = "Updated UserCards table";

            try {
                UpdateUserCardsResponse response = userCardService
                        .updateUserCards(userKey, userCardsToUpsert)
                        .block();

                assertAll(
                        () -> assertEquals(expectedResponseStatus, response.getStatus()),
                        () -> assertEquals(expectedResponseMessage, response.getMessage())
                );

                List<UserCard> allCardsResponse = userCardService
                        .getUserCardsByUserId(userKey, userId)
                        .block();

                assertEquals(expectedNumberOfUserCards, allCardsResponse.size());

                List<UserCard> userCard1UpdatedResponse = userCardService
                        .getUserCardByCardId(userKey, expectedUserCardUpdated1.getCardId())
                        .block();

                userCard1UpdatedResponse.forEach(uc -> {
                    String cardIdReturned = uc.getCardId();
                    String userIdReturned = uc.getUserId().toString();
                    int quantityReturned = uc.getQuantity();

                    assertAll(
                            () -> assertEquals(expectedUserCardUpdated1.getCardId(), cardIdReturned),
                            () -> assertEquals(expectedUserCardUpdated1.getUserId().toString(), userIdReturned),
                            () -> assertEquals(expectedUserCardUpdated1.getQuantity(), quantityReturned)
                    );
                });

                List<UserCard> userCard2UpdatedResponse = userCardService
                        .getUserCardByCardId(userKey, expectedUserCardUpdated2.getCardId())
                        .block();

                userCard2UpdatedResponse.forEach(uc -> {
                    String cardIdReturned = uc.getCardId();
                    String userIdReturned = uc.getUserId().toString();
                    int quantityReturned = uc.getQuantity();

                    assertAll(
                            () -> assertEquals(expectedUserCardUpdated2.getCardId(), cardIdReturned),
                            () -> assertEquals(expectedUserCardUpdated2.getUserId().toString(), userIdReturned),
                            () -> assertEquals(expectedUserCardUpdated2.getQuantity(), quantityReturned)
                    );
                });

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                deleteTestUser();
                boolean failed = true;
                assertFalse(failed);
            }
        }

        @Test
        void userCardService_UpsertAndDeleteUserCards_ReturnCorrectStatusCodeAndMessage() {
            UUID userIdToSend = UUID.fromString(userId);

            List<UserCard> userCardsToUpsertAndDelete = List.of(
                    new UserCard("card1", userIdToSend, 1),
                    new UserCard("card2", userIdToSend, 10),
                    new UserCard("card11", userIdToSend, 1),
                    new UserCard("card20", userIdToSend, 1),
                    new UserCard("card4", userIdToSend, 0),
                    new UserCard("card3", userIdToSend, 0)
            );

            int expectedNumberOfUserCards = 5;
            UserCard expectedUserCardUpdated1 = new UserCard(
                    "card1",
                    UUID.fromString(userId),
                    1
            );

            UserCard expectedUserCardUpdated2 = new UserCard(
                    "card2",
                    UUID.fromString(userId),
                    10
            );

            String expectedResponseStatus = "200";
            String expectedResponseMessage = "Updated UserCards table";

            String userCardDeletedId1 = "card3";
            String userCardDeletedId2 = "card4";

            try {

                List<UserCard> userCard1DeletedBefore = userCardService
                        .getUserCardByCardId(userKey, userCardDeletedId1)
                        .block();

                List<UserCard> userCard2DeletedBefore = userCardService
                        .getUserCardByCardId(userKey, userCardDeletedId2)
                        .block();

                assertAll(
                        () -> assertEquals(1, userCard1DeletedBefore.size()),
                        () -> assertEquals(1, userCard2DeletedBefore.size())
                );

                UpdateUserCardsResponse response = userCardService
                        .updateUserCards(userKey, userCardsToUpsertAndDelete)
                        .block();

                assertAll(
                        () -> assertEquals(expectedResponseStatus, response.getStatus()),
                        () -> assertEquals(expectedResponseMessage, response.getMessage())
                );

                List<UserCard> allCardsResponse = userCardService
                        .getUserCardsByUserId(userKey, userId)
                        .block();

                assertEquals(expectedNumberOfUserCards, allCardsResponse.size());

                List<UserCard> userCard1UpdatedResponse = userCardService
                        .getUserCardByCardId(userKey, expectedUserCardUpdated1.getCardId())
                        .block();

                userCard1UpdatedResponse.forEach(uc -> {
                    String cardIdReturned = uc.getCardId();
                    String userIdReturned = uc.getUserId().toString();
                    int quantityReturned = uc.getQuantity();

                    assertAll(
                            () -> assertEquals(expectedUserCardUpdated1.getCardId(), cardIdReturned),
                            () -> assertEquals(expectedUserCardUpdated1.getUserId().toString(), userIdReturned),
                            () -> assertEquals(expectedUserCardUpdated1.getQuantity(), quantityReturned)
                    );
                });

                List<UserCard> userCard2UpdatedResponse = userCardService
                        .getUserCardByCardId(userKey, expectedUserCardUpdated2.getCardId())
                        .block();

                userCard2UpdatedResponse.forEach(uc -> {
                    String cardIdReturned = uc.getCardId();
                    String userIdReturned = uc.getUserId().toString();
                    int quantityReturned = uc.getQuantity();

                    assertAll(
                            () -> assertEquals(expectedUserCardUpdated2.getCardId(), cardIdReturned),
                            () -> assertEquals(expectedUserCardUpdated2.getUserId().toString(), userIdReturned),
                            () -> assertEquals(expectedUserCardUpdated2.getQuantity(), quantityReturned)
                    );
                });

                List<UserCard> userCard1DeletedAfter = userCardService
                        .getUserCardByCardId(userKey, userCardDeletedId1)
                        .block();

                List<UserCard> userCard2DeletedAfter = userCardService
                        .getUserCardByCardId(userKey, userCardDeletedId2)
                        .block();

                assertAll(
                        () -> assertEquals(0, userCard1DeletedAfter.size()),
                        () -> assertEquals(0, userCard2DeletedAfter.size())
                );

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                deleteTestUser();
                boolean failed = true;
                assertFalse(failed);
            }
        }

        @Test
        void userCardService_UpsertAndDeleteUserCardsWithAnInvalidUserCard_ReturnsCorrectErrorStatusAndMessage() {
            UUID randomUserId = UUID.randomUUID();
            UUID userIdToSend = UUID.fromString(userId);

            List<UserCard> userCardsToUpsertAndDelete = List.of(
                    new UserCard("card1", userIdToSend, 1),
                    new UserCard("card2", randomUserId, 10),
                    new UserCard("card11", userIdToSend, 1),
                    new UserCard("card20", randomUserId, 1),
                    new UserCard("card4", userIdToSend, 0),
                    new UserCard("card3", userIdToSend, 0)
            );

            String expectedErrorCode = "500";
            String expectedErrorMessage = "couldn't update UserCards table, caused by: " +
                    "All UserCards must have the same UserId!";

            try {
                UpdateUserCardsResponse response = userCardService
                        .updateUserCards(userKey, userCardsToUpsertAndDelete)
                        .block();

                assertAll(
                        () -> assertEquals(expectedErrorCode, response.getStatus()),
                        () -> assertEquals(expectedErrorMessage, response.getMessage())
                );

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                deleteTestUser();
                boolean failed = true;
                assertFalse(failed);
            }
        }

        @Test
        void userCardService_GetUserCardNotInTable_ReturnsEmptyArray() {
            String invalidId = "invalid";

            try {
                List<UserCard> response = userCardService
                        .getUserCardByCardId(userKey, invalidId)
                        .block();

                assertEquals(0, response.size());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                deleteTestUser();
                boolean failed = true;
                assertFalse(failed);
            }
        }

        @Test
        void userCardService_GetUserCardWithUserIdNotInTable_ReturnsEmptyArray() {
            String invalidUser = UUID.randomUUID().toString();

            try {
                List<UserCard> response = userCardService
                        .getUserCardsByUserId(userKey, invalidUser)
                        .block();

                assertEquals(0, response.size());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                deleteTestUser();
                boolean failed = true;
                assertFalse(failed);
            }
        }
    }
}
