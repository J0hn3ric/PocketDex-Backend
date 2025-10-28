package org.example.PocketDex.ServiceTests;


import io.github.cdimascio.dotenv.Dotenv;
import org.example.PocketDex.DTO.response.ResponseBodyDTO;
import org.example.PocketDex.DTO.response.UpdateUserCardsResponseDTO;
import org.example.PocketDex.DTO.response.UserCardWithCardInfoResponseDTO;
import org.example.PocketDex.Model.User;
import org.example.PocketDex.Model.UserCard;
import org.example.PocketDex.Rarity;
import org.example.PocketDex.Service.UserCardService;
import org.example.PocketDex.Service.UserService;
import org.example.PocketDex.ServiceTests.helpers.UserServiceHelper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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

    @BeforeAll
    static void loadEnv() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
    }

    private User user;
    private String backendToken;
    private String userId;

    @BeforeEach
    void configureUser() {
        ResponseBodyDTO<List<User>> createUserResponse = UserServiceHelper.createNewUser(userService);

        user = createUserResponse.data()
                .getFirst();

        backendToken = createUserResponse.backendToken();

        userId = user.getId().toString();
    }

    @AfterEach
    void deleteTestUser() {
        if (backendToken != null) {
            userService.deleteUserUsingBackendToken(backendToken).block();
        } else {
            System.out.println("no backend token set!!");
        }
    }

    @Test
    void userCardService_GetUserCardsWhenTableIsEmpty_ReturnsEmptyArray() {
        try {
            ResponseBodyDTO<List<UserCardWithCardInfoResponseDTO>> response = userCardService
                    .getUserCardsByUserId(backendToken, userId)
                    .block();

            assertNotNull(response);

            List<UserCardWithCardInfoResponseDTO> userCardListReturned = response.data();

            assertEquals(0, userCardListReturned.size());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            deleteTestUser();
            boolean failed = true;
            assertFalse(failed);
        }
    }

    @Test
    void userCardService_AddOneUserCardToDB_CardAddedToDb() {
        int expectedUpdatedCardsSize = 1;
        int expectedDeletedCardsSize = 0;

        int quantityExpected = 2;
        String cardIdExpected = "A1-001";
        int cardImgLengthExpected = 97;
        String cardNameExpected = "Bulbasaur";
        String cardPackExpected = "A1-Mewtwo_Pack";
        Rarity cardRarityExpected = Rarity.ONE_DIA;
        String expansionExpected = "A1";

        UserCard userCardRequest = new UserCard(
            cardIdExpected,
            quantityExpected
        );

        try {

            ResponseBodyDTO<UpdateUserCardsResponseDTO> response = userCardService.updateUserCards(
                    backendToken,
                    List.of(userCardRequest)
            ).block();

            assertNotNull(response);

            List<String> updatedUserCards = response.data().updatedUserCards();
            List<String> deletedUserCards = response.data().deletedUserCards();


            assertAll(
                    () -> assertEquals(expectedUpdatedCardsSize, updatedUserCards.size()),
                    () -> assertEquals(expectedDeletedCardsSize, deletedUserCards.size())
            );

            ResponseBodyDTO<List<UserCardWithCardInfoResponseDTO>> getResponse = userCardService
                    .getOwnedUserCards(backendToken)
                    .block();

            assertNotNull(getResponse);
            assertEquals(backendToken, getResponse.backendToken());

            List<UserCardWithCardInfoResponseDTO> userCardsInDB = getResponse.data();

            assertAll(
                    () -> assertEquals(
                            cardIdExpected,
                            userCardsInDB.getFirst()
                                    .cardInfo()
                                    .getId()
                    ),
                    () -> assertEquals(
                            cardNameExpected,
                            userCardsInDB.getFirst()
                                    .cardInfo()
                                    .getName()
                    ),
                    () -> assertEquals(
                            cardImgLengthExpected,
                            userCardsInDB.getFirst()
                                    .cardInfo()
                                    .getRes()
                                    .length()
                    ),
                    () -> assertEquals(
                            cardPackExpected,
                            userCardsInDB.getFirst()
                                    .cardInfo()
                                    .getPackId()
                    ),
                    () -> assertEquals(
                            cardRarityExpected,
                            userCardsInDB.getFirst()
                                    .cardInfo()
                                    .getRarity()
                    ),
                    () -> assertEquals(
                            expansionExpected,
                            userCardsInDB.getFirst()
                                    .cardInfo()
                                    .getExpansion()
                    ),
                    () -> assertEquals(quantityExpected, userCardsInDB.getFirst().quantity()),
                    () -> assertEquals(userCardRequest.isTradable(), userCardsInDB.getFirst().isTradable())
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
                    new UserCard("A1-001", 3),
                    new UserCard("A1-002", 5),
                    new UserCard("A1-003", 7),
                    new UserCard("A1-004", 38),
                    new UserCard("A1-005", 1)
            );
            userCardService.updateUserCards(backendToken, userCardsToUpdate).block();
        }

        @Test
        void userCardService_DeleteSomeUserCards_ReturnsDeletedUserCardsAndAreDeletedFromDb() {
            UUID userIdToSend = UUID.fromString(userId);

            List<UserCard> userCardsToDelete = List.of(
                    new UserCard("A1-001", 0),
                    new UserCard("A1-005", -2)
            );

            int expectedUpdatedCardsSize = 0;
            int expectedDeletedCardsSize = 2;
            int expectedNumberOfUserCardsInDb = 3;


            try {
                ResponseBodyDTO<UpdateUserCardsResponseDTO> response = userCardService
                        .updateUserCards(backendToken, userCardsToDelete)
                        .block();

                assertNotNull(response);

                List<String> updatedUserCardsList = response.data().updatedUserCards();
                List<String> deletedUserCardsList = response.data().deletedUserCards();

                assertAll(
                        () -> assertEquals(expectedUpdatedCardsSize, updatedUserCardsList.size()),
                        () -> assertEquals(expectedDeletedCardsSize, deletedUserCardsList.size()),
                        () -> {
                            for (int i = 0; i < deletedUserCardsList.size(); i++) {
                                assertEquals(userCardsToDelete.get(i).getCardId(), deletedUserCardsList.get(i));
                            }
                        }
                );

                ResponseBodyDTO<List<UserCardWithCardInfoResponseDTO>> getResponse = userCardService
                        .getOwnedUserCards(backendToken)
                        .block();

                assertNotNull(getResponse);
                assertEquals(backendToken, getResponse.backendToken());

                List<UserCardWithCardInfoResponseDTO> userCardsInDb = getResponse.data();

                assertEquals(expectedNumberOfUserCardsInDb, userCardsInDb.size());
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
            String userCardIdToLookFor = "A1-001";
            UserCard expectedUserCard = new UserCard(
                    userCardIdToLookFor,
                    UUID.fromString(userId),
                    3
            );

            try {
                ResponseBodyDTO<List<UserCard>> response = userCardService.getUserCardByCardId(
                        backendToken,
                        userCardIdToLookFor
                ).block();

                assertNotNull(response);
                assertEquals(backendToken, response.backendToken());

                List<UserCard> responseData = response.data();

                responseData.forEach(uc -> {
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
        void userCardService_UpsertSomeUserCards_ReturnsUpsertedUserCardsAndAreUpsertedInDb() {
            int expectedNumberOfUserCardsUpserted = 4;
            int expectedNumberOfUserCardsDeleted = 0;

            UUID userIdToSend = UUID.fromString(userId);

            List<UserCard> userCardsToUpsert = List.of(
                    new UserCard("A1-001", 1),
                    new UserCard("A1-002", 10),
                    new UserCard("A1-011", 1),
                    new UserCard("A1-020", 1)
            );

            int expectedNumberOfUserCards = 7;
            UserCard expectedUserCardUpdated1 = new UserCard(
                    "A1-001",
                    userIdToSend,
                    1
            );

            UserCard expectedUserCardUpdated2 = new UserCard(
                    "A1-02",
                    userIdToSend,
                    10
            );

            String expectedResponseStatus = "200";
            String expectedResponseMessage = "Updated UserCards table";

            try {
                ResponseBodyDTO<UpdateUserCardsResponseDTO> response = userCardService
                        .updateUserCards(backendToken, userCardsToUpsert)
                        .block();

                assertNotNull(response);
                assertEquals(backendToken, response.backendToken());

                UpdateUserCardsResponseDTO responseData = response.data();

                assertAll(
                        () -> assertEquals(
                                expectedNumberOfUserCardsUpserted,
                                responseData.updatedUserCards().size()
                        ),
                        () -> assertEquals(
                                expectedNumberOfUserCardsDeleted,
                                responseData.deletedUserCards().size()
                        )
                );


                ResponseBodyDTO<List<UserCardWithCardInfoResponseDTO>> allCardsResponse = userCardService
                        .getOwnedUserCards(backendToken)
                        .block();

                assertNotNull(allCardsResponse);

                List<UserCardWithCardInfoResponseDTO> allCardsResponseData = allCardsResponse.data();

                assertEquals(expectedNumberOfUserCards, allCardsResponseData.size());

                ResponseBodyDTO<List<UserCard>> userCard1UpdatedResponse = userCardService
                        .getUserCardByCardId(backendToken, expectedUserCardUpdated1.getCardId())
                        .block();

                assertNotNull(userCard1UpdatedResponse);

                List<UserCard> userCard1Updated = userCard1UpdatedResponse.data();

                userCard1Updated.forEach(uc -> {
                    String cardIdReturned = uc.getCardId();
                    String userIdReturned = uc.getUserId().toString();
                    int quantityReturned = uc.getQuantity();

                    assertAll(
                            () -> assertEquals(expectedUserCardUpdated1.getCardId(), cardIdReturned),
                            () -> assertEquals(expectedUserCardUpdated1.getUserId().toString(), userIdReturned),
                            () -> assertEquals(expectedUserCardUpdated1.getQuantity(), quantityReturned)
                    );
                });

                ResponseBodyDTO<List<UserCard>> userCard2UpdatedResponse = userCardService
                        .getUserCardByCardId(backendToken, expectedUserCardUpdated2.getCardId())
                        .block();

                assertNotNull(userCard2UpdatedResponse);

                List<UserCard> userCard2Updated = userCard2UpdatedResponse.data();

                userCard2Updated.forEach(uc -> {
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
            int expectedNumberOfUpdatedUserCards = 4;
            int expectedNumberOfDeletedUserCards = 2;

            UUID userIdToSend = UUID.fromString(userId);

            List<UserCard> userCardsToUpsertAndDelete = List.of(
                    new UserCard("A1-001", 1),
                    new UserCard("A1-002", 10),
                    new UserCard("A1-011", 1),
                    new UserCard("A1-020", 1),
                    new UserCard("A1-004", 0),
                    new UserCard("A1-003", 0)
            );

            int expectedNumberOfUserCards = 5;
            UserCard expectedUserCardUpdated1 = new UserCard(
                    "A1-001",
                    userIdToSend,
                    1
            );

            UserCard expectedUserCardUpdated2 = new UserCard(
                    "A1-002",
                    userIdToSend,
                    10
            );

            String userCardDeletedId1 = "A1-003";
            String userCardDeletedId2 = "A1-004";

            try {

                ResponseBodyDTO<List<UserCard>> userCard1DeletedBeforeResponse = userCardService
                        .getUserCardByCardId(backendToken, userCardDeletedId1)
                        .block();

                assertNotNull(userCard1DeletedBeforeResponse);
                assertEquals(backendToken, userCard1DeletedBeforeResponse.backendToken());

                ResponseBodyDTO<List<UserCard>> userCard2DeletedBeforeResponse = userCardService
                        .getUserCardByCardId(backendToken, userCardDeletedId2)
                        .block();

                assertNotNull(userCard2DeletedBeforeResponse);
                assertEquals(backendToken, userCard2DeletedBeforeResponse.backendToken());

                List<UserCard> userCard1DeletedBefore = userCard1DeletedBeforeResponse.data();
                List<UserCard> userCard2DeletedBefore = userCard2DeletedBeforeResponse.data();

                assertAll(
                        () -> assertEquals(1, userCard1DeletedBefore.size()),
                        () -> assertEquals(1, userCard2DeletedBefore.size())
                );

                ResponseBodyDTO<UpdateUserCardsResponseDTO> updateUserCardsResponse = userCardService
                        .updateUserCards(backendToken, userCardsToUpsertAndDelete)
                        .block();

                assertNotNull(updateUserCardsResponse);
                assertEquals(backendToken, updateUserCardsResponse.backendToken());

                UpdateUserCardsResponseDTO userCardsEdited = updateUserCardsResponse.data();

                assertAll(
                        () -> assertEquals(
                                expectedNumberOfUpdatedUserCards,
                                userCardsEdited.updatedUserCards().size()
                        ),
                        () -> assertEquals(
                                expectedNumberOfDeletedUserCards,
                                userCardsEdited.deletedUserCards().size()
                        )
                );

                ResponseBodyDTO<List<UserCardWithCardInfoResponseDTO>> allCardsResponse = userCardService
                        .getUserCardsByUserId(backendToken, userId)
                        .block();

                assertNotNull(allCardsResponse);
                assertEquals(backendToken, allCardsResponse.backendToken());

                List<UserCardWithCardInfoResponseDTO> allCards = allCardsResponse.data();

                assertEquals(expectedNumberOfUserCards, allCards.size());

                ResponseBodyDTO<List<UserCard>> userCard1UpdatedResponse = userCardService
                        .getUserCardByCardId(backendToken, expectedUserCardUpdated1.getCardId())
                        .block();

                assertNotNull(userCard1UpdatedResponse);
                assertEquals(backendToken,userCard1UpdatedResponse.backendToken());

                List<UserCard> userCard1UpdatedAfter = userCard1UpdatedResponse.data();

                userCard1UpdatedAfter.forEach(uc -> {
                    String cardIdReturned = uc.getCardId();
                    String userIdReturned = uc.getUserId().toString();
                    int quantityReturned = uc.getQuantity();

                    assertAll(
                            () -> assertEquals(expectedUserCardUpdated1.getCardId(), cardIdReturned),
                            () -> assertEquals(expectedUserCardUpdated1.getUserId().toString(), userIdReturned),
                            () -> assertEquals(expectedUserCardUpdated1.getQuantity(), quantityReturned)
                    );
                });

                ResponseBodyDTO<List<UserCard>> userCard2UpdatedResponse = userCardService
                        .getUserCardByCardId(backendToken, expectedUserCardUpdated2.getCardId())
                        .block();

                assertNotNull(userCard2UpdatedResponse);
                assertEquals(backendToken, userCard2UpdatedResponse.backendToken());

                List<UserCard> userCard2UpdatedAfter = userCard2UpdatedResponse.data();

                userCard2UpdatedAfter.forEach(uc -> {
                    String cardIdReturned = uc.getCardId();
                    String userIdReturned = uc.getUserId().toString();
                    int quantityReturned = uc.getQuantity();

                    assertAll(
                            () -> assertEquals(expectedUserCardUpdated2.getCardId(), cardIdReturned),
                            () -> assertEquals(expectedUserCardUpdated2.getUserId().toString(), userIdReturned),
                            () -> assertEquals(expectedUserCardUpdated2.getQuantity(), quantityReturned)
                    );
                });

                ResponseBodyDTO<List<UserCard>> userCard1DeletedResponse = userCardService
                        .getUserCardByCardId(backendToken, userCardDeletedId1)
                        .block();

                ResponseBodyDTO<List<UserCard>> userCard2DeletedResponse = userCardService
                        .getUserCardByCardId(backendToken, userCardDeletedId2)
                        .block();

                assertNotNull(userCard1DeletedResponse);
                assertNotNull(userCard2DeletedResponse);

                List<UserCard> userCard1DeletedAfter = userCard1DeletedResponse.data();
                List<UserCard> userCard2DeletedAfter = userCard2DeletedResponse.data();

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
        void userCardService_GetUserCardNotInTable_ReturnsEmptyArray() {
            String invalidId = "invalid";

            try {
                ResponseBodyDTO<List<UserCard>> response = userCardService
                        .getUserCardByCardId(backendToken, invalidId)
                        .block();

                assertNotNull(response);

                List<UserCard> responseData = response.data();

                assertEquals(0, responseData.size());
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
                ResponseBodyDTO<List<UserCardWithCardInfoResponseDTO>> response = userCardService
                        .getUserCardsByUserId(backendToken, invalidUser)
                        .block();

                assertNotNull(response);

                List<UserCardWithCardInfoResponseDTO> responseData = response.data();

                assertEquals(0, responseData.size());
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

