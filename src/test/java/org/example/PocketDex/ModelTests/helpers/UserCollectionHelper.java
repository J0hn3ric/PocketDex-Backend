package org.example.PocketDex.ModelTests.helpers;

import org.example.PocketDex.Model.UserCard;
import org.example.PocketDex.Model.UserCollection;

import java.util.ArrayList;
import java.util.UUID;

public class UserCollectionHelper {

    public static ArrayList<UserCard> generateUserCardList() {
        String[] cardIdArray = {"1", "2", "3"};
        int[] quantityArray = {1, 2, 2};
        UUID userId = UUID.randomUUID();
        ArrayList<UserCard> userCardList = new ArrayList<UserCard>();

        for (int i = 0; i < 3; i++) {
            UserCard newUserCard = new UserCard(
                    cardIdArray[i],
                    userId,
                    quantityArray[i]
            );

            userCardList.add(newUserCard);
        }

        return userCardList;
    }

    public static UserCollection generateEmptyCollection() {
        UUID userId = UUID.randomUUID();

        UserCollection userCollection = new UserCollection(
                userId
        );

        return userCollection;
    }

    public static ArrayList<UserCard> generateNewUserCardList() {
        String[] cardIdArray = {"4", "5", "3"};
        int[] quantityArray = {1, 2, 5};
        UUID userId = UserHelper.userId;
        ArrayList<UserCard> newUserCardList = new ArrayList<UserCard>();

        for (int i = 0; i < 3; i++) {
            UserCard newUserCard = new UserCard(
                    cardIdArray[i],
                    userId,
                    quantityArray[i]
            );

            newUserCardList.add(newUserCard);
        }

        return newUserCardList;
    }

    public static UserCollection generateFilledCollection() {
        ArrayList<UserCard> userCardList = new ArrayList<UserCard>(
                UserCollectionHelper.generateUserCardList()
        );
        UUID userId = UserHelper.userId;

        UserCollection userCollection = new UserCollection(
                userId,
                userCardList
        );

        return  userCollection;
    }
}
