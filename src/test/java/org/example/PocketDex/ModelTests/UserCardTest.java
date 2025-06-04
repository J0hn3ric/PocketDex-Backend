package org.example.PocketDex.ModelTests;

import org.example.PocketDex.Model.UserCard;
import org.example.PocketDex.ModelTests.helpers.UserCardHelper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserCardTest {

    @Test
    public void userCard_Initialization_InstanceConstructedCorrectly() {
        String cardId = "1";
        int quantity = 1;
        UUID userId = UUID.randomUUID();

        UserCard userCard = new UserCard(
                cardId,
                userId,
                quantity
        );

        assertAll(
                () -> assertEquals(cardId, userCard.getCardId()),
                () -> assertEquals(quantity, userCard.getQuantity()),
                () -> assertEquals(userId, userCard.getUserId())
        );
    }

    @Test
    public void userCard_AfterGeneratingTradeableUserCard_UserCardIsTradeable() {
        UserCard tradeableCard = UserCardHelper.generateTradeableUserCard();

        assertTrue(tradeableCard.isTradable());
    }

    @Test
    public void userCard_GenerateNonTradeableCardAndUpdateQuantity_QuantityIsUpdatedAndCardIsTradeable() {
        UserCard userCard = UserCardHelper.generateNonTradeableUserCard();
        int quantity = userCard.getQuantity();

        assertAll(
                () -> assertEquals(quantity, userCard.getQuantity()),
                () -> assertFalse(userCard.isTradable())
        );

        int newQuantity = 5;
        userCard.updateQuantity(newQuantity);

        assertAll(
                () -> assertEquals(newQuantity, userCard.getQuantity()),
                () -> assertTrue(userCard.isTradable())
        );
    }
}
