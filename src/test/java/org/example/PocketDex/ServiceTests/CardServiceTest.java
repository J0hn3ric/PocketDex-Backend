package org.example.PocketDex.ServiceTests;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.PocketDex.DTO.response.UserCardWithCardInfoResponseDTO;
import org.example.PocketDex.Model.UserCard;
import org.example.PocketDex.Rarity;
import org.example.PocketDex.Model.Card;
import org.example.PocketDex.Repository.CardRepository;
import org.example.PocketDex.Service.CardService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CardServiceTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardService cardService;

    @BeforeAll
    static void loadEnv() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
    }

    @Test
    public void cardService_GetUserCardsWithInfoWithValidUserCards_UserCardsEnrichedWithInfoReturned() {
        UUID userId = UUID.randomUUID();
        int expectedUserCardListSize = 3;

        List<UserCard> userCardList = List.of(
                new UserCard("A1-001", userId, 1),
                new UserCard("A1-002", userId, 1),
                new UserCard("A1-003", userId, 1)
        );

        List<UserCardWithCardInfoResponseDTO> userCardEnriched = cardService
                .getUserCardsWithInfo(userCardList)
                .collectList()
                .block();

        assertNotNull(userCardEnriched);
        assertEquals(expectedUserCardListSize, userCardEnriched.size());

        for (int i = 0; i < expectedUserCardListSize; i++) {
            int finalI = i;
            assertAll(
                    () -> assertEquals(userCardList.get(finalI).getQuantity(), userCardEnriched.get(finalI).quantity()),
                    () -> assertNotNull(userCardEnriched.get(finalI).cardInfo()),
                    () -> assertEquals(userCardList.get(finalI).getCardId(), userCardEnriched.get(finalI).cardInfo().getId())
            );
        }
    }

    public void cardService_GetUserCardsWithInfoWithAnInvalidUserCard_ReturnsLessUserCardEnrichedThanGiven() {
        UUID userId = UUID.randomUUID();
        int expectedUserCardListSize = 2;

        List<UserCard> userCardList = List.of(
                new UserCard("A1-001", userId, 1),
                new UserCard("invalid", userId, 1),
                new UserCard("A1-003", userId, 1)
        );

        List<UserCardWithCardInfoResponseDTO> userCardEnriched = cardService
                .getUserCardsWithInfo(userCardList)
                .collectList()
                .block();

        assertNotNull(userCardEnriched);
        assertNotEquals(userCardList.size(), userCardEnriched.size());
        assertEquals(expectedUserCardListSize, userCardEnriched.size());
    }

    @Test
    public void cardService_GetPaginatedCards_CardLengthIsCorrect() {
        int numberOfDocuments = 50;
        String expactedPrefix = "A1-";

        List<Card> allCards = cardService
                .getPaginatedCards("A1-001")
                .block();

        assertNotNull(allCards);

        assertEquals(numberOfDocuments, allCards.size());

        AtomicInteger cardNumber = new AtomicInteger(1);

        System.out.println("Raw from Mongo:");
        allCards.forEach(c -> System.out.println(c.getId()));

        allCards.forEach(c -> {
            assertEquals(expactedPrefix + String.format("%03d", cardNumber.get()), c.getId());
            cardNumber.getAndIncrement();
        });
    }

    @Test
    public void cardService_getCardById_CardInstanceCreatedSuccessfully() {
        String cardId = "A1-001";
        int expectedLengthOfImgUrl = 97;

        Card cardToBeQueried = new Card(
                "A1-001",
                "suca",
                "Bulbasaur",
                Rarity.ONE_DIA,
                "A1-Mewtwo_Pack",
                "A1"
        );

        try {
            Card card1 = cardService
                    .getCardById(cardId)
                    .block();

            assertNotNull(card1);

            assertAll(
                    () -> assertEquals(cardToBeQueried.getId(), card1.getId()),
                    () -> assertEquals(expectedLengthOfImgUrl, card1.getRes().length()),
                    () -> assertEquals(cardToBeQueried.getName(), card1.getName()),
                    () -> assertEquals(cardToBeQueried.getRarity(), card1.getRarity()),
                    () -> assertEquals(cardToBeQueried.getPackId(), card1.getPackId()),
                    () -> assertEquals(cardToBeQueried.getExpansion(), card1.getExpansion())
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            boolean failed = true;
            assertFalse(failed);
        }
    }

    @Test
    void cardService_GetCardByExpansion_CorrectLengthOfListQueried() {
        String expansion = "A1";
        int numberOfCardsInA1 = 286;

        try {
            List<Card> cardList = cardService
                    .getCardByExpansion(expansion)
                    .block();

            assertNotNull(cardList);

            assertAll(
                    () -> assertEquals(numberOfCardsInA1, cardList.size()),
                    () -> cardList.forEach( c ->
                            assertEquals(expansion, c.getExpansion())
                    )
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            boolean failed = true;
            assertFalse(failed);
        }
    }

    @Test
    void cardService_GetPaginatedCardsByExpansion_CorrectLengthOfListQueried() {
        String expansion = "A1";
        int cardListLengthExpected = 50;

        try {
            List<Card> paginatedCardList = cardService
                    .getPaginatedCardByExpansion(expansion, "A1-001")
                    .block();

            assertNotNull(paginatedCardList);

            assertAll(
                    () -> assertEquals(cardListLengthExpected, paginatedCardList.size()),
                    () -> paginatedCardList.forEach( c ->
                            assertEquals(expansion, c.getExpansion())
                    )
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            boolean failed = true;
            assertFalse(failed);
        }
    }

    @Test
    void cardService_GetCardByName_CardListQueriedCorrectly() {
        String name = "Bulbasaur";
        int numberOfBulbasaurs = 3;

        try {
            List<Card> cardList = cardService
                    .getCardByName(name)
                    .block();

            assertNotNull(cardList);

            assertAll(
                    () -> assertEquals(numberOfBulbasaurs, cardList.size()),
                    () -> cardList.forEach(c ->
                            assertEquals(name, c.getName())
                    )
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            boolean failed = true;
            assertFalse(failed);
        }
    }

    @Test
    void cardService_GetCardByPack_CardListQueriedCorrectly() {
        String pack = "A1-Mewtwo_Pack";
        int numberOfCards = 79;

        try {
            List<Card> cardList = cardService
                    .getCardByPack(pack)
                    .block();

            assertNotNull(cardList);

            assertAll(
                    () -> assertEquals(numberOfCards, cardList.size()),
                    () -> cardList.forEach( c ->
                            assertEquals(pack, c.getPackId())
                    )
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            boolean failed = true;
            assertFalse(failed);
        }
    }

    @Test
    void cardService_GetPaginatedCardByPack_CardListQueriedCorrectly() {
        String pack = "A1-Mewtwo_Pack";
        int cardListLengthExpected = 50;

        try {
            List<Card> paginatedCardList = cardService
                    .getPaginatedCardByPack(pack, "A1-001")
                    .block();

            assertNotNull(paginatedCardList);

            assertAll(
                    () -> assertEquals(cardListLengthExpected, paginatedCardList.size()),
                    () -> paginatedCardList.forEach( c ->
                            assertEquals(pack, c.getPackId())
                    )
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            boolean failed = true;
            assertFalse(failed);
        }
    }

    @Test
    void cardService_GetCardByRarity_CardListQueriedCorrectly() {
        Rarity cardRarity = Rarity.CROWN;
        String cardRarityString = cardRarity.getRarityString();
        int numberOfCrownCards = 10;

        try {
            List<Card> cardList = cardService
                    .getPaginatedCardByRarity(cardRarityString, "A1-001")
                    .block();

            assertNotNull(cardList);

            assertAll(
                    () -> assertEquals(numberOfCrownCards, cardList.size()),
                    () -> cardList.forEach( c ->
                            assertEquals(cardRarity, c.getRarity())
                    )
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            boolean failed = true;
            assertFalse(failed);
        }
    }

    @Test
    void cardService_GetCardByIdDoesNotExist_ReturnsNull() {
        String notValidId = "12";

        Card card = cardService
                .getCardById(notValidId)
                .block();

        assertNull(card);
    }

    @Test
    void cardService_GetCardByInvalidExpansion_ReturnsEmptyList() {
        int expectedCardListLength = 0;
        String invalidExpansion = "invalidExpansion";

        List<Card> cardList = cardService
                .getCardByExpansion(invalidExpansion)
                .block();

        assertNotNull(cardList);
        assertEquals(expectedCardListLength, cardList.size());
    }

    @Test
    void cardService_GetPaginatedCardByInvalidExpansion_ReturnsEmptyList() {
        int expectedPaginatedCardListLength = 0;
        String invalidExpansion = "invalidExpansion";

        List<Card> paginatedCardList = cardService
                .getPaginatedCardByExpansion(invalidExpansion, "A1-001")
                .block();

        assertNotNull(paginatedCardList);
        assertEquals(expectedPaginatedCardListLength, paginatedCardList.size());
    }

    @Test
    void cardService_GetCardByInvalidName_ReturnsEmptyList() {
        int expectedCardListLength = 0;
        String invalidName = "invalidName";

        List<Card> cardList = cardService
                .getCardByName(invalidName)
                .block();

        assertNotNull(cardList);
        assertEquals(expectedCardListLength, cardList.size());
    }


    @Test
    void cardService_GetCardByInvalidPack_ReturnsEmptyList() {
        int expectedCardListLength = 0;
        String invalidPack = "invalidPack";

        List<Card> cardList = cardService
                .getCardByPack(invalidPack)
                .block();

        assertNotNull(cardList);
        assertEquals(expectedCardListLength, cardList.size());
    }

    @Test
    void cardService_GetPaginatedCardByInvalidPack_ReturnsEmptyList() {
        int expectedCardListLength = 0;
        String invalidPack = "invalidPack";

        List<Card> paginatedCardList = cardService
                .getPaginatedCardByPack(invalidPack, "A1-001")
                .block();

        assertNotNull(paginatedCardList);
        assertEquals(expectedCardListLength, paginatedCardList.size());
    }
}
