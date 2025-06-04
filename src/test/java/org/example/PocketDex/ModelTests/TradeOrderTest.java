package org.example.PocketDex.ModelTests;

import org.example.PocketDex.ModelTests.helpers.*;
import org.example.PocketDex.TradeStatus;
import org.example.PocketDex.Model.TradeOffer;
import org.example.PocketDex.Model.TradeOrder;
import org.example.PocketDex.Model.UserCard;
import org.example.PocketDex.Model.Card;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TradeOrderTest {

    @Test
    public void tradeOrder_InitializeWithDataFromDB_InstanceCreatedCorrectly() {
        Long id = Long.valueOf(156512315231655L);
        UUID userId = UserHelper.userId;
        TradeStatus status = TradeStatus.ACTIVE;
        Boolean visible = true;
        ZonedDateTime createdTime = ZonedDateTime.now();
        Long tradeOffer = null;
        ArrayList<UserCard> userCardsToTrade = new ArrayList<UserCard>(
                TradeOrderHelper.generateListOfUserCardsToTrade()
        );
        ArrayList<Card> cardsToTrade = new ArrayList<Card>(
                TradeOrderHelper.generateListOfCardsToTrade()
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
                visible,
                createdTime,
                null,
                status,
                userCardsToTradeId,
                cardsToTradeId,
                tradeOffer
        );

        assertAll(
                () -> assertEquals(id, tradeOrder.getId()),
                () -> assertEquals(userId, tradeOrder.getUserId()),
                () -> assertEquals(status, tradeOrder.getStatus()),
                () -> assertEquals(visible, tradeOrder.getVisible()),
                () -> assertTrue(createdTime.equals(tradeOrder.getCreatedTime())),
                () -> assertNull(tradeOrder.getTradeOfferId()),
                () -> assertEquals(userCardsToTradeId.size(), tradeOrder.getCardsToSendIds().size()),
                () -> assertEquals(cardsToTradeId.size(), tradeOrder.getCardsToReceiveIds().size())
        );
    }

    @Test
    public void tradeOrder_InitializeWithVisibilityFalse_InstanceCreatedSuccesfully() {
        Long id = Long.valueOf(156512315231655L);
        UUID userId = UserHelper.userId;
        boolean visibility = false;
        ArrayList<UserCard> userCardsToTrade = new ArrayList<UserCard>(
                TradeOrderHelper.generateListOfUserCardsToTrade()
        );
        ArrayList<Card> cardsToTrade = new ArrayList<Card>(
                TradeOrderHelper.generateListOfCardsToTrade()
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
                visibility,
                userCardsToTradeId,
                cardsToTradeId
        );

        assertAll(
                () -> assertEquals(id, tradeOrder.getId()),
                () -> assertEquals(userId, tradeOrder.getUserId()),
                () -> assertEquals(visibility, tradeOrder.getVisible()),
                () -> assertNull(tradeOrder.getCompletedTime()),
                () -> assertEquals(TradeStatus.PAUSED, tradeOrder.getStatus()),
                () -> assertNull(tradeOrder.getTradeOfferId()),
                () -> assertNotNull(tradeOrder.getCreatedTime()),
                () -> assertEquals(userCardsToTradeId.size(), tradeOrder.getCardsToSendIds().size()),
                () -> assertEquals(cardsToTradeId.size(), tradeOrder.getCardsToReceiveIds().size())
        );
    }

    @Test
    public void tradeOrder_InitializeWithVisibilityTrue_InstanceCreatedSuccesfully() {
        Long id = Long.valueOf(156512315231655L);
        UUID userId = UserHelper.userId;
        boolean visibility = true;
        ArrayList<UserCard> userCardsToTrade = new ArrayList<UserCard>(
                TradeOrderHelper.generateListOfUserCardsToTrade()
        );
        ArrayList<Card> cardsToTrade = new ArrayList<Card>(
                TradeOrderHelper.generateListOfCardsToTrade()
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
                visibility,
                userCardsToTradeId,
                cardsToTradeId
        );

        assertAll(
                () -> assertEquals(id, tradeOrder.getId()),
                () -> assertEquals(userId, tradeOrder.getUserId()),
                () -> assertEquals(visibility, tradeOrder.getVisible()),
                () -> assertNull(tradeOrder.getCompletedTime()),
                () -> assertEquals(TradeStatus.ACTIVE, tradeOrder.getStatus()),
                () -> assertNull(tradeOrder.getTradeOfferId()),
                () -> assertNotNull(tradeOrder.getCreatedTime()),
                () -> assertEquals(userCardsToTradeId.size(), tradeOrder.getCardsToSendIds().size()),
                () -> assertEquals(cardsToTradeId.size(), tradeOrder.getCardsToReceiveIds().size())
        );
    }

    @Test
    public void tradeOrder_InitializeWithOnlyCards_InstanceCreatedSuccesfully() {
        Long id = Long.valueOf(156512315231655L);
        UUID userId = UserHelper.userId;
        ArrayList<UserCard> userCardsToTrade = new ArrayList<UserCard>(
                TradeOrderHelper.generateListOfUserCardsToTrade()
        );
        ArrayList<Card> cardsToTrade = new ArrayList<Card>(
                TradeOrderHelper.generateListOfCardsToTrade()
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

        assertAll(
                () -> assertEquals(id, tradeOrder.getId()),
                () -> assertEquals(userId, tradeOrder.getUserId()),
                () -> assertEquals(userCardsToTradeId.size(), tradeOrder.getCardsToSendIds().size()),
                () -> assertEquals(cardsToTradeId.size(), tradeOrder.getCardsToReceiveIds().size()),
                () -> assertFalse(tradeOrder.getVisible()),
                () -> assertNull(tradeOrder.getCompletedTime()),
                () -> assertNotNull(tradeOrder.getCreatedTime()),
                () -> assertEquals(TradeStatus.PAUSED, tradeOrder.getStatus()),
                () -> assertNull(tradeOrder.getTradeOfferId())
        );
    }

    @Test
    public void tradeOrder_AddCardToReceiveToList_CardAddedToList() {
        TradeOrder tradeOrder = TradeOrderHelper.generateNewTradeOrder();

        String newCardToreceive = CardHelper.generateNewCard().getId();

        int cardToReceiveListLengthBefore = tradeOrder
                .getCardsToReceiveIds()
                .size();

        ArrayList<String> cardListToAdd = new ArrayList<String>(List.of(newCardToreceive));

        boolean result = tradeOrder.addCardsToReceiveToList(cardListToAdd);

        int cardToReceiveListLengthAfter = tradeOrder
                .getCardsToReceiveIds()
                .size();

        assertEquals(
                cardToReceiveListLengthBefore + 1,
                cardToReceiveListLengthAfter
        );

        assertTrue(result);
    }

    @Test
    public void tradeOrder_RemoveCardToReceiveFromList_CardRemovedFromListCorrectly() {
        TradeOrder tradeOrder = TradeOrderHelper.generateNewTradeOrder();
        String cardToRemove = TradeOrderHelper.generateCardIdToBeRemoved();

        int cardToReceiveListLengthBefore = tradeOrder
                .getCardsToReceiveIds()
                .size();

        ArrayList<String> cardToRemoveList = new ArrayList<String>(List.of(cardToRemove));

        boolean result = tradeOrder.removeCardsToReceiveFromList(cardToRemoveList);

        int cardToReceiveListLengthAfter = tradeOrder
                .getCardsToReceiveIds()
                .size();

        assertEquals(
                cardToReceiveListLengthBefore - 1,
                cardToReceiveListLengthAfter
        );

        assertTrue(result);
    }

    @Test
    public void tradeOrder_AddUserCardToSendToList_UserCardAddedToList() {
        TradeOrder tradeOrder = TradeOrderHelper.generateNewTradeOrder();
        String newUserCard = UserCardHelper.generateNonTradeableUserCard().getCardId();

        int cardToSendListLengthBefore = tradeOrder
                .getCardsToSendIds()
                .size();

        ArrayList<String> listToAdd = new ArrayList<String>(List.of(newUserCard));

        boolean result = tradeOrder.addUserCardsToSendToList(listToAdd);

        int cardToSendListLengthAfter = tradeOrder
                .getCardsToSendIds()
                .size();

        assertEquals(
                cardToSendListLengthBefore + 1,
                cardToSendListLengthAfter
        );

        assertTrue(result);
    }

    @Test
    public void tradeOrder_RemoveUserCardToSendFromList_UserCardRemovedFromListCorrectly() {
        TradeOrder tradeOrder = TradeOrderHelper.generateNewTradeOrder();
        String userCardToRemove = TradeOrderHelper.generateUserCardIdToBeRemoved();

        int cardToSendListLengthBefore = tradeOrder
                .getCardsToSendIds()
                .size();

        ArrayList<String> cardListToRemove = new ArrayList<String>(List.of(userCardToRemove));

        boolean result = tradeOrder.removeUserCardsToSendFromList(cardListToRemove);

        int cardToSendListLengthAfter = tradeOrder
                .getCardsToSendIds()
                .size();

        assertEquals(
                cardToSendListLengthBefore - 1,
                cardToSendListLengthAfter
        );

        assertTrue(result);
    }

    @Test
    public void tradeOrder_AddCardToReceiveAlreadyInListToList_ListRemainsTheSame() {
        TradeOrder tradeOrder = TradeOrderHelper.generateNewTradeOrder();
        String cardAlreadyInList = TradeOrderHelper.generateCardIdToBeRemoved();

        int cardToReceiveListLengthBefore = tradeOrder
                .getCardsToReceiveIds()
                .size();

        ArrayList<String> cardListToAdd = new ArrayList<String>(List.of(cardAlreadyInList));

        boolean result = tradeOrder.addCardsToReceiveToList(cardListToAdd);

        int cardToReceiveListLengthAfter = tradeOrder
                .getCardsToReceiveIds()
                .size();

        assertAll(
                () -> assertEquals(
                        cardToReceiveListLengthBefore,
                        cardToReceiveListLengthAfter
                ),
                () -> assertTrue(result)
        );
    }


    @Test
    public void tradeOrder_RemoveCardToReceiveAlreadyNotInListFromList_ListRemainsTheSame() {
        TradeOrder tradeOrder = TradeOrderHelper.generateNewTradeOrder();
        String cardAlreadyNotInList = CardHelper.generateNewCard().getId();

        int cardToReceiveListLengthBefore = tradeOrder
                .getCardsToReceiveIds()
                .size();

        ArrayList<String> cardToRemoveList = new ArrayList<String>(List.of(cardAlreadyNotInList));

        boolean result = tradeOrder.removeCardsToReceiveFromList(cardToRemoveList);

        int cardToReceiveListLengthAfter = tradeOrder
                .getCardsToReceiveIds()
                .size();

        assertAll(
                () -> assertEquals(
                        cardToReceiveListLengthBefore,
                        cardToReceiveListLengthAfter
                ),
                () -> assertTrue(result)
        );
    }

    @Test
    public void tradeOrder_AddUserCardToSendAlreadyInListToList_ListRemainsTheSame() {
        TradeOrder tradeOrder = TradeOrderHelper.generateNewTradeOrder();
        String cardAlreadyInList = TradeOrderHelper.generateUserCardIdToBeRemoved();

        int cardToSendListLengthBefore = tradeOrder
                .getCardsToSendIds()
                .size();

        ArrayList<String> listToAdd = new ArrayList<String>(List.of(cardAlreadyInList));

        boolean result = tradeOrder.addUserCardsToSendToList(listToAdd);

        int cardToSendListLengthAfter = tradeOrder
                .getCardsToSendIds()
                .size();

        assertAll(
                () -> assertEquals(
                        cardToSendListLengthBefore,
                        cardToSendListLengthAfter
                ),
                () -> assertTrue(result)
        );
    }

    @Test
    public void tradeOrder_RemoveUserCardToSendAlreadyNotInListFromList_ListRemainsTheSame() {
        TradeOrder tradeOrder = TradeOrderHelper.generateNewTradeOrder();
        String cardAlreadyNotInList = UserCardHelper.generateNonTradeableUserCard().getCardId();

        int cardToSendListLengthBefore = tradeOrder
                .getCardsToSendIds()
                .size();

        ArrayList<String> cardListToRemove = new ArrayList<String>(List.of(cardAlreadyNotInList));

        boolean result = tradeOrder.removeUserCardsToSendFromList(cardListToRemove);

        int cardToSendListLengthAfter = tradeOrder
                .getCardsToSendIds()
                .size();

        assertAll(
                () -> assertEquals(
                        cardToSendListLengthBefore,
                        cardToSendListLengthAfter
                ),
                () -> assertTrue(result)
        );
    }

    @Test
    public void tradeOrder_ToggleVisibility_VisibilityAndStatusChanged() {
        TradeOrder tradeOrder = TradeOrderHelper.generateNewTradeOrder();

        boolean visibilityBefore = tradeOrder.getVisible();
        TradeStatus tradeStatusBefore = tradeOrder.getStatus();

        tradeOrder.toggleVisibility(true);

        boolean visibilityAfter = tradeOrder.getVisible();
        TradeStatus tradeStatusAfter = tradeOrder.getStatus();

        assertAll(
                () -> assertNotEquals(visibilityBefore, visibilityAfter),
                () -> assertNotEquals(tradeStatusBefore, tradeStatusAfter)
        );
    }

    @Test
    public void tradeOrder_AssociateTradeOffer_TradeOfferIdFilled() {
        TradeOrder tradeOrder = TradeOrderHelper.generateNewTradeOrder();

        assertNull(tradeOrder.getTradeOfferId());

        TradeOffer tradeOffer = TradeOfferHelper.generateNewTradeOffer(tradeOrder);
        tradeOrder.associateTradeOffer(tradeOffer);

        assertEquals(tradeOffer.getId(), tradeOrder.getTradeOfferId());
    }
}
