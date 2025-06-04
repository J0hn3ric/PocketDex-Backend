package org.example.PocketDex.ModelTests.helpers;

import org.example.PocketDex.Model.UserCard;

import java.util.UUID;

public class UserCardHelper {

    public static UserCard generateTradeableUserCard() {
        String cardId = "1";
        int quantity = 2;
        UUID userId = UserHelper.userId;

        UserCard userCard = new UserCard(
                cardId,
                userId,
                quantity
        );

        return  userCard;
    }

    public static UserCard generateNonTradeableUserCard() {
        String cardId = "1";
        int quantity = 1;
        UUID userId = UserHelper.userId;

        UserCard userCard = new UserCard(
                cardId,
                userId,
                quantity
        );

        return  userCard;
    }
}
