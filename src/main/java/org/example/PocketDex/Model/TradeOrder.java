package org.example.PocketDex.Model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.PocketDex.TradeStatus;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
public class TradeOrder {

    private Long id;
    private UUID userId;
    private Boolean visible = false;
    private ZonedDateTime completedTime = null;
    private ZonedDateTime createdTime;
    private TradeStatus status = TradeStatus.PAUSED;
    private ArrayList<String> cardsToSendIds; // IDs of UserCard To Send
    private ArrayList<String> cardsToReceiveIds; // IDs of Cards to receive
    private Long tradeOfferId = null;

    public boolean isCompleted() {
        return this.completedTime != null;
    }

    // Constructor for Database
    public TradeOrder(
            Long id,
            UUID userId,
            Boolean visible,
            ZonedDateTime createdTime,
            ZonedDateTime completedTime,
            TradeStatus status,
            ArrayList<String> cardsToSendIds,
            ArrayList<String> cardsToReceiveIds,
            Long tradeOfferId
    ) {
        this.id = id;
        this.userId = userId;
        this.visible = visible;
        this.createdTime = createdTime;
        this.completedTime = completedTime;
        this.status = status;
        this.cardsToSendIds = new ArrayList<String>(
                cardsToSendIds
        );
        this.cardsToReceiveIds = new ArrayList<String>(
                cardsToReceiveIds
        );
        this.tradeOfferId = tradeOfferId;
    }

    public TradeOrder(
            Long id,
            UUID userId,
            Boolean visible,
            ArrayList<String> cardsToSendIds,
            ArrayList<String> cardsToReceiveIds
            ) {
        this.id = id;
        this.userId = userId;
        this.visible = visible;
        this.createdTime = ZonedDateTime.now();
        this.status = visible
                ? TradeStatus.ACTIVE
                : TradeStatus.PAUSED;

        this.cardsToSendIds = new ArrayList<String>(
                cardsToSendIds
        );

        this.cardsToReceiveIds = new ArrayList<String>(
                cardsToReceiveIds
        );
    }

    public TradeOrder(
            Long id,
            UUID userId,
            ArrayList<String> cardsToSendIds,
            ArrayList<String> cardsToReceiveIds
    ) {
        this.id = id;
        this.userId = userId;
        this.createdTime = ZonedDateTime.now();

        this.cardsToSendIds = new ArrayList<String>(
                cardsToSendIds
        );

        this.cardsToReceiveIds = new ArrayList<String>(
                cardsToReceiveIds
        );
    }

    public boolean addUserCardsToSendToList (ArrayList<String> userCardList) {
        int lengthOfListBefore = this.cardsToSendIds.size();
        int cardsAddedToListCount = 0;
        for (String userCardId : userCardList) {
            if (checkIfUserCardNotInList(userCardId)) {
                this.cardsToSendIds.add(userCardId);
                cardsAddedToListCount += 1;
            }
        }

        return this.cardsToSendIds.size() == lengthOfListBefore + cardsAddedToListCount;
    }

    public boolean removeUserCardsToSendFromList(ArrayList<String> userCardList) {
        int lengthOfListBefore = this.cardsToSendIds.size();
        int cardsRemovedFromListCount = 0;

        for (String userCard : userCardList) {
            if (!checkIfUserCardNotInList(userCard)) {
                this.cardsToSendIds.remove(userCard);

                cardsRemovedFromListCount += 1;
            }
        }

        return  this.cardsToSendIds.size() == lengthOfListBefore - cardsRemovedFromListCount;
    }

    public boolean checkIfUserCardNotInList(String userCardId) {
        String cardInList = findUserCardbyId(userCardId);

        return cardInList == null;
    }

    private String findUserCardbyId(String userCardId) {
        return this.cardsToSendIds.stream()
                .filter(c -> c.equals(userCardId))
                .findFirst()
                .orElse(null);
    }

    public boolean addCardsToReceiveToList(ArrayList<String> cardList) {
        int lengthOfListBefore = this.cardsToReceiveIds.size();
        int cardsAddedToListCount = 0;

        for (String card : cardList) {
            if (checkIfCardNotInList(card)) {
                this.cardsToReceiveIds.add(card);

                cardsAddedToListCount += 1;
            }
        }

        return  this.cardsToReceiveIds.size() == lengthOfListBefore + cardsAddedToListCount;
    }

    public boolean removeCardsToReceiveFromList(ArrayList<String> cardList) {
        int lengthOfListBefore = this.cardsToReceiveIds.size();
        int cardsRemovedFromListCount = 0;

        for (String card : cardList) {
            if (!checkIfCardNotInList(card)) {
                this.cardsToReceiveIds.remove(card);

                cardsRemovedFromListCount += 1;
            }
        }

        return  this.cardsToReceiveIds.size() == lengthOfListBefore - cardsRemovedFromListCount;
    }

    public boolean checkIfCardNotInList(String cardId) {
        String cardInList = findCardbyId(cardId);

        return cardInList == null;
    }

    private String findCardbyId(String cardId) {
        return  this.cardsToReceiveIds.stream()
                .filter(c -> c.equals(cardId))
                .findFirst()
                .orElse(null);
    }

    public void toggleVisibility(boolean visible) {
        if (!isCompleted() && this.tradeOfferId == null) {
            this.visible = visible;

            this.status = this.visible
                    ? TradeStatus.ACTIVE
                    : TradeStatus.PAUSED;
        }
    }

    public void associateTradeOffer(
            TradeOffer tradeOffer
    ) {
        this.tradeOfferId = tradeOffer.getId();
    }


    /* Service will do this
    public void completeTrade(Long userId, TradeOffer tradeOffer) throws Exception {
        if (tradeOffer == null) {
            throw new Exception("Trade Offer Not Created Yet");
        }

        if (userId.equals(this.tradeOffer.getUserSendingId())) {
            this.tradeOffer.senderConfirms();
        } else if (userId.equals(this.tradeOffer.getUserReceivingId())) {
            this.tradeOffer.receiverConfirms();
        } else {
            throw new Exception("User Id given are not part of the trade");
        }

        if (tradeOffer.isCompleted() && !isCompleted()) {
            this.status = TradeStatus.COMPLETED;
            this.completedTime = ZonedDateTime.now();
            toggleVisibility(false);
        }
    }
    */

}
