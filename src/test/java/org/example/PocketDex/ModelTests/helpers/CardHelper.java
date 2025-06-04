package org.example.PocketDex.ModelTests.helpers;

import org.example.PocketDex.Model.Card;

public class CardHelper {

    public static Card generateNewCard() {
        String id = "5";
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

            return cardTest;
        } catch (Exception e) {
            return null;
        }
    }

}
