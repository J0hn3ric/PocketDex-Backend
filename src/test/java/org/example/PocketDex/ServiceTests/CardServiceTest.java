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

    @Test
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

        System.out.println(userCardEnriched);

        assertNotNull(userCardEnriched);
        assertNotEquals(userCardList.size(), userCardEnriched.size());
        assertEquals(expectedUserCardListSize, userCardEnriched.size());
    }

    @Test
    public void cardService_GetPaginatedCards_CardLengthIsCorrect() {
        int numberOfDocuments = 50;
        String expactedPrefix = "A1-";

        List<Card> allCards = cardService
                .getPaginatedCards(
                        "A1-001",
                        null,
                        null,
                        null,
                        null
                )
                .block();

        assertNotNull(allCards);

        assertEquals(numberOfDocuments, allCards.size());

        AtomicInteger cardNumber = new AtomicInteger(1);

        allCards.forEach(c -> {
            assertEquals(expactedPrefix + String.format("%03d", cardNumber.get()), c.getId());
            cardNumber.getAndIncrement();
        });
    }

    @Test
    public void cardService_GetPaginatedCardsByRarity_CardLengthIsCorrectAndQueryExecutedCorrectly() {
        int numberOfDocuments = 50;
        String expectedRarity = Rarity.ONE_DIA.getRarityString();

        List<Card> filteredCards = cardService
                .getPaginatedCards(
                        "A1-001",
                        expectedRarity,
                        null,
                        null,
                        null
                )
                .block();

        assertNotNull(filteredCards);

        assertEquals(numberOfDocuments, filteredCards.size());

        filteredCards.forEach(c ->
                assertEquals(Rarity.ONE_DIA, c.getRarity())
        );
    }

    @Test
    public void cardService_GetPaginatedCardsByExpansion_CardLengthIsCorrectAndQueryExecutedCorrectly() {
        int numberOfDocuments = 50;
        String expectedExpansion = "A1a";

        List<Card> filteredCards = cardService
                .getPaginatedCards(
                        "A1-001",
                        null,
                        null,
                        expectedExpansion,
                        null
                )
                .block();

        assertNotNull(filteredCards);

        assertEquals(numberOfDocuments, filteredCards.size());

        filteredCards.forEach(c ->
                assertEquals(expectedExpansion, c.getExpansion())
        );
    }

    @Test
    public void cardService_GetPaginatedCardsByPack_CardLengthIsCorrectAndQueryExecutedCorrectly() {
        int numberOfDocuments = 50;
        String expectedPack = "A1-Charizard_Pack";

        List<Card> filteredCards = cardService
                .getPaginatedCards(
                        "A1-001",
                        null,
                        null,
                        null,
                        expectedPack
                )
                .block();

        assertNotNull(filteredCards);

        assertEquals(numberOfDocuments, filteredCards.size());

        filteredCards.forEach(c ->
                assertEquals(expectedPack, c.getPackId())
        );
    }

    @Test
    public void cardService_GetPaginatedCardsByExactName_CardLengthIsCorrectAndQueryExecutedCorrectly() {
        int numberOfDocuments = 3;
        String expectedName = "bulbasaur";

        List<Card> filteredCards = cardService
                .getPaginatedCards(
                        "A1-001",
                        null,
                        expectedName,
                        null,
                        null
                )
                .block();

        assertNotNull(filteredCards);

        assertEquals(numberOfDocuments, filteredCards.size());

        filteredCards.forEach(c ->
                assertEquals(expectedName, c.getName().toLowerCase())
        );
    }

    @Test
    public void cardService_GetPaginatedCardsByExactNamePrefix_CardLengthIsCorrectAndQueryExecutedCorrectly() {
        String expectedNamePrefix = "m";

        List<Card> filteredCards = cardService
                .getPaginatedCards(
                        "A1-001",
                        null,
                        expectedNamePrefix,
                        null,
                        null
                )
                .block();

        assertNotNull(filteredCards);

        filteredCards.forEach(c ->
                assertEquals(expectedNamePrefix, c.getName().substring(0, expectedNamePrefix.length()).toLowerCase())
        );
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
    public void cardService_GetPaginatedCardsByInvalidRarity_ReturnsEmptyList() {
        int numberOfDocuments = 0;
        String expectedRarity = "error";

        List<Card> filteredCards = cardService
                .getPaginatedCards(
                        "A1-001",
                        expectedRarity,
                        null,
                        null,
                        null
                )
                .block();

        assertNotNull(filteredCards);

        assertEquals(numberOfDocuments, filteredCards.size());
    }

    @Test
    public void cardService_GetPaginatedCardsByInvalidExpansion_ReturnsEmptyList() {
        int numberOfDocuments = 0;
        String expectedExpansion = "error";

        List<Card> filteredCards = cardService
                .getPaginatedCards(
                        "A1-001",
                        null,
                        null,
                        expectedExpansion,
                        null
                )
                .block();

        assertNotNull(filteredCards);

        assertEquals(numberOfDocuments, filteredCards.size());
    }

    // to trigger pull request

    @Test
    public void cardService_GetPaginatedCardsByInvalidPack_ReturnsEmptyList() {
        int numberOfDocuments = 0;
        String expectedPack = "error";

        List<Card> filteredCards = cardService
                .getPaginatedCards(
                        "A1-001",
                        null,
                        null,
                        null,
                        expectedPack
                )
                .block();

        assertNotNull(filteredCards);

        assertEquals(numberOfDocuments, filteredCards.size());
    }
}
