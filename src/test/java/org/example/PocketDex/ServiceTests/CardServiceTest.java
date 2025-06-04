package org.example.PocketDex.ServiceTests;

import io.github.cdimascio.dotenv.Dotenv;
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
    public void cardService_GetPaginatedCards_CardLengthIsCorrect() {
        int numberOfDocuments = 50;
        String expactedPrefix = "A1";

        List<Card> allCards = cardService
                .getPaginatedCards("A1-1", 50)
                .block();

        assertEquals(numberOfDocuments, allCards.size());

        AtomicInteger cardNumber = new AtomicInteger(1);

        allCards.forEach(c -> {
            assertEquals(expactedPrefix + cardNumber, c.getId());
            cardNumber.getAndIncrement();
        });
    }

    @Test
    public void cardService_getCardById_CardInstanceCreatedSuccesfully() {
        String cardId = "A1-1";

        Card cardToBeQueried = new Card(
                "A1-1",
                "https://storage.googleapis.com/pocketdeximages.firebasestorage.app/A1_Images/A1_001_EN.webp",
                "Bulbasaur",
                Rarity.ONE_DIA,
                "A1-Mewtwo_Pack",
                "A1"
        );

        try {
            Optional<Card> card1 = cardService.getCardById(cardId);

            assertAll(
                    () -> assertInstanceOf(Card.class, card1.get()),
                    () -> assertEquals(cardToBeQueried.getId(), card1.get().getId()),
                    () -> assertEquals(cardToBeQueried.getRes(), card1.get().getRes()),
                    () -> assertEquals(cardToBeQueried.getName(), card1.get().getName()),
                    () -> assertEquals(cardToBeQueried.getRarity(), card1.get().getRarity()),
                    () -> assertEquals(cardToBeQueried.getPackId(), card1.get().getPackId()),
                    () -> assertEquals(cardToBeQueried.getExpansion(), card1.get().getExpansion())
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
            List<Card> cardList = cardService.getCardByExpansion(expansion);

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
    void cardService_GetCardByName_CardListQueriedCorrectly() {
        String name = "Bulbasaur";
        int numberOfBulbasaurs = 3;

        try {
            List<Card> cardList = cardService.getCardByName(name);

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
            List<Card> cardList = cardService.getCardByPack(pack);

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

    /*
    @Test
    void cardService_GetCardByRarity_CardListQueriedCorrectly() {
        Rarity cardRarity = Rarity.CROWN;
        String cardRarityString = cardRarity.getRarityString();
        int numberOfCrownCards = 10;

        try {
            List<Card> cardList = cardService.getCardByRarity(cardRarityString);

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
     */

    @Test
    void cardService_GetCardByIdDoesNotExist_ThrowsException() {
        String notValidId = "12";

        assertThrows(
                Exception.class,
                () -> cardService.getCardById(notValidId)
        );
    }

    @Test
    void cardService_GetCardByInvalidExpansion_ThrowsException() {
        String invalidExpansion = "invalidExpansion";

        assertThrows(
                Exception.class,
                () -> cardService.getCardByExpansion(invalidExpansion)
        );
    }

    @Test
    void cardService_GetCardByInvalidName_ThrowsExcpetion() {
        String invalidName = "invalidName";

        assertThrows(
                Exception.class,
                () -> cardService.getCardByName(invalidName)
        );
    }

    @Test
    void cardService_GetCardByInvalidPack_ThrowsException() {
        String invalidPack = "invalidPack";

        assertThrows(
                Exception.class,
                () -> cardService.getCardByPack(invalidPack)
        );
    }
}
