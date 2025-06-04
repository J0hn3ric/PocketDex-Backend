package org.example.PocketDex.ModelTests;

import org.example.PocketDex.Model.UserCollection;
import org.example.PocketDex.Model.UserCard;
import org.example.PocketDex.ModelTests.helpers.UserCollectionHelper;
import org.example.PocketDex.ModelTests.helpers.UserHelper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*public class UserCollectionTest {

    @Test
    public void collection_Initialization_InstanceCreatedCorrectly() {
        ArrayList<UserCard> userCardList = UserCollectionHelper.generateUserCardList();
        Long userId = UserHelper.userId;

        UserCollection userCollection = new UserCollection(
                userId,
                userCardList
        );

        assertAll(
                () -> assertEquals(userId, userCollection.getUserId()),
                () -> assertEquals(userCardList.size(), userCollection.getUserCardsToUpdate().size())
        );
    }


    @Test
    public void collection_InitializationWithNoList_InstanceCreatedWithEmptyList() {
        Long userId = UserHelper.userId;

        UserCollection userCollection = new UserCollection(
                userId
        );

        assertAll(
                () -> assertEquals(userId, userCollection.getUserId()),
                () -> assertEquals(0, userCollection.getUserCardsToUpdate().size()),
                () -> assertEquals(0, userCollection.getUserCardsToRemove().size())
        );
    }


    @Test
    public void collection_AddNewCardsToCardList_CardsAddedToList() {
        UserCollection userCollection = UserCollectionHelper.generateEmptyCollection();

        assertEquals(0, userCollection.getUserCardsToUpdate().size());
        assertEquals(0, userCollection.getUserCardsToRemove().size());

        ArrayList<UserCard> newUserCardsList = UserCollectionHelper.generateUserCardList();

        userCollection.(newUserCardsList);

        assertEquals(newUserCardsList.size(), userCollection.getCardList().size());
    }


    @Test
    public void collection_UpdateCardList_NewCardsAddedToListCardAlreadyInListUpdateQuantity() {
        // Generate User Collection
        ArrayList<UserCard> userCardList = new ArrayList<UserCard>(
                UserCollectionHelper.generateUserCardList()
        );
        Long userId = UserHelper.userId;

        UserCollection userCollection = new UserCollection(
                userId,
                userCardList
        );

        assertEquals(userCardList.size(), userCollection.getCardList().size());

        // Save information on UserCard to be updated
        String toUpdateUserCardId = "3";
        int toUpdateUserCardQuantity = userCollection
                .findCardbyId(toUpdateUserCardId)
                .getQuantity();

        // Generate new UserCard List
        ArrayList<UserCard> newUserCardList = new ArrayList<UserCard>(
                UserCollectionHelper.generateNewUserCardList()
        );
        userCollection.updateCollection(newUserCardList);

        // Count new UserCards in UserCard List
        int newUserCardsCount = newUserCardList.stream()
                .filter(c -> !c.getCardId().equals(toUpdateUserCardId))
                .toList()
                .size();

        UserCard updatedUserCard = userCollection.findCardbyId(toUpdateUserCardId);

        assertAll(
                () -> assertEquals(
                        userCardList.size() + newUserCardsCount,
                        userCollection.getCardList().size()
                ),
                () -> assertNotEquals(
                        toUpdateUserCardQuantity,
                        updatedUserCard.getQuantity()
                ),
                () -> assertEquals(
                        5,
                        updatedUserCard.getQuantity()
                )
        );
    }

    @Test
    public void collection_UpdateExistingCardWithInvalidQuantity_ReturnsFalse() {
        UserCollection userCollection = UserCollectionHelper.generateFilledCollection();

        ArrayList<UserCard> cardListWithInvalidQuantity = new ArrayList<UserCard>();

        UserCard userCardWithInvalidQuantity = new UserCard(
                "1",
                userCollection.getUserId(),
                -7
        );

        cardListWithInvalidQuantity.add(userCardWithInvalidQuantity);

        assertFalse(userCollection.updateCollection(cardListWithInvalidQuantity));
    }

    @Test
    public void collection_AddNewCardWithInvalidQuantity_ReturnsFalse() {
        UserCollection userCollection = UserCollectionHelper.generateFilledCollection();

        ArrayList<UserCard> cardListWithInvalidQuantity = new ArrayList<UserCard>();

        UserCard userCardWithInvalidQuantity = new UserCard(
                "6",
                userCollection.getUserId(),
                -7
        );

        cardListWithInvalidQuantity.add(userCardWithInvalidQuantity);

        assertFalse(userCollection.updateCollection(cardListWithInvalidQuantity));
    }

    @Test
    public void collection_UpdateExistingUserCardQuantityToZero_RemovesCardFromCollection() {
        UserCollection userCollection = UserCollectionHelper.generateFilledCollection();
        UserCard cardWithZeroQuantity = new UserCard(
                "1",
                userCollection.getUserId(),
                0
        );

        ArrayList<UserCard> collectionBeforeRemoving = new ArrayList<UserCard>(
                userCollection.getCardList()
        );

        userCollection.updateCollection(
                new ArrayList<UserCard>(List.of(cardWithZeroQuantity))
        );

        assertEquals(
                collectionBeforeRemoving.size() - 1,
                userCollection.getCardList().size()
        );
    }


}*/
