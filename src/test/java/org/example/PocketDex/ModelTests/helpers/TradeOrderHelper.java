package org.example.PocketDex.ModelTests.helpers;

import org.example.PocketDex.Model.Card;
import org.example.PocketDex.Model.TradeOrder;
import org.example.PocketDex.Model.UserCard;
import org.example.PocketDex.Rarity;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class TradeOrderHelper {

    public static ArrayList<UserCard> generateListOfUserCardsToTrade() {
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

    public static ArrayList<Card> generateListOfCardsToTrade() {
        String[] cardIdArray = {"1", "2", "7"};
        String[] resArray = {"1", "2", "7"};
        String[] nameArray = {"a", "b", "c"};
        Rarity[] rarityArray = {Rarity.FOUR_DIA, Rarity.FOUR_DIA, Rarity.FOUR_DIA};
        String[] packIdArray = {"c", "c", "c"};
        String[] expansionArray = {"e", "e", "e"};

        ArrayList<Card> cardsToTrade = new ArrayList<Card>();

        for (int i = 0; i < 3; i++) {
            Card newCard = new Card(
                    cardIdArray[i],
                    resArray[i],
                    nameArray[i],
                    rarityArray[i],
                    packIdArray[i],
                    expansionArray[i]
            );

            cardsToTrade.add(newCard);
        }

        return  cardsToTrade;
    }

    public static TradeOrder generateNewTradeOrder() {
        Long id = Long.valueOf(156512315231655L);
        UUID userId = UserHelper.userId;
        ArrayList<UserCard> userCardsToTrade = new ArrayList<UserCard>(
                generateListOfUserCardsToTrade()
        );
        ArrayList<Card> cardsToTrade = new ArrayList<Card>(
                generateListOfCardsToTrade()
        );

        ArrayList<String> userCardsToTradeId = new ArrayList<String>();

        for (UserCard userCard : userCardsToTrade) {
            userCardsToTradeId.add(userCard.getCardId());
        }

        ArrayList<String> cardsToTradeId = new ArrayList<String>();

        for (Card card : cardsToTrade) {
            cardsToTradeId.add(card.getId());
        }

        TradeOrder tradeOrder = new TradeOrder(
                id,
                userId,
                userCardsToTradeId,
                cardsToTradeId
        );

        return tradeOrder;
    }

    public static String generateCardIdToBeRemoved() {
        String id = "1";

        return id;
    }

    public static String generateUserCardIdToBeRemoved() {
        String cardId = "4";

        return  cardId;
    }
}
