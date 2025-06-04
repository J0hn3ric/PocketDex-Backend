package org.example.PocketDex.Model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TradeOffer {
    Long id;
    boolean senderConfirmed = false; // confirmation of user who created the TradeOffer
    boolean receiverConfirmed = false; // confirmation of user who created the TradeOrder
    String cardToSendId; // card sent by the user who created the TradeOrder
    String cardToReceiveId; // card sent by the user who created the TradeOffer
    UUID userReceivingId; // user who created the TradeOrder
    UUID userSendingId; // user who created the TradeOffer
    Long tradeOrderId;

    // from db
    public TradeOffer(
            Long id,
            boolean senderConfirmed,
            boolean receiverConfirmed,
            String cardToSendId,
            String cardToReceiveId,
            UUID userReceivingId,
            UUID userSendingId,
            Long tradeOrderId
    ) {
        this.id = id;
        this.senderConfirmed = senderConfirmed;
        this.receiverConfirmed = receiverConfirmed;
        this.cardToSendId = cardToSendId;
        this.cardToReceiveId = cardToReceiveId;
        this.userReceivingId = userReceivingId;
        this.userSendingId = userSendingId;
        this.tradeOrderId = tradeOrderId;
    }

    public TradeOffer(
            String cardToSendId,
            String cardToReceiveId,
            UUID userReceivingId,
            UUID userSendingId,
            TradeOrder tradeOrder
    ) {
        this.tradeOrderId = tradeOrder.getTradeOfferId();
        this.cardToSendId = cardToSendId;
        this.cardToReceiveId = cardToReceiveId;
        this.userReceivingId = userReceivingId;
        this.userSendingId = userSendingId;
    }

    public void senderConfirms() {
        if (!this.senderConfirmed) {
            this.senderConfirmed = true;
        }
    }

    public void receiverConfirms() {
        if (!this.receiverConfirmed) {
            this.receiverConfirmed = true;
        }
    }

    public boolean isCompleted() {
        if (this.senderConfirmed && this.receiverConfirmed) {
            return true;
        }
        return false;
    }
}
