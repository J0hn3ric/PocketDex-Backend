package org.example.PocketDex.ModelTests;

import org.example.PocketDex.Model.Card;
import org.example.PocketDex.Rarity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CardTest {

    @Test
    public void card_InitializationWithRarityAsString_InstanceConstructedCorrectly() {
        String id = "1";
        String res = "abc";
        String name = "test-name";
        String rarity = "1-Dia"; // so Rarity enum is ONE_DIA
        String packId = "test-pack";
        String expansion = "test-expansion";

        try {
            Card cardTest = new Card(
                    id,
                    res,
                    name,
                    rarity,
                    packId,
                    expansion
            );

            assertEquals(id, cardTest.getId());
            assertEquals(res, cardTest.getRes());
            assertEquals(name, cardTest.getName());
            assertEquals(Rarity.ONE_DIA, cardTest.getRarity());
            assertEquals(packId, cardTest.getPackId());
            assertEquals(expansion, cardTest.getExpansion());
        } catch (Exception e) {

        }
    }

    @Test
    public void card_InitializationWithRarityAsRarityEnum_InstanceConstructedCorrectly() {
        String id = "1";
        String res = "abc";
        String name = "test-name";
        Rarity rarity = Rarity.ONE_DIA; // so Rarity enum is ONE_DIA
        String packId = "test-pack";
        String expansion = "test-expansion";
        Card cardTest = new Card(
                id,
                res,
                name,
                rarity,
                packId,
                expansion
        );

        assertEquals(id, cardTest.getId());
        assertEquals(res, cardTest.getRes());
        assertEquals(name, cardTest.getName());
        assertEquals(rarity, cardTest.getRarity());
        assertEquals(packId, cardTest.getPackId());
        assertEquals(expansion, cardTest.getExpansion());
    }

    @Test
    public  void card_InitializationWithWrongRarityAsString_ExceptionThrown() {
        String id = "1";
        String res = "abc";
        String name = "test-name";
        String rarity = "error-rarity";
        String packId = "test-pack";
        String expansion = "test-expansion";

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    Card cardTest = new Card(
                            id,
                            res,
                            name,
                            rarity,
                            packId,
                            expansion
                    );
                }
        );
    }
}
