package org.example.PocketDex.ModelTests.helpers;

import org.example.PocketDex.Model.TradeOffer;
import org.example.PocketDex.Model.TradeOrder;

import java.util.UUID;

public class TradeOfferHelper {

    public static TradeOffer generateNewTradeOffer(TradeOrder tradeOrder) {
        String cardToSendId = "4";

        String cardToReceiveId = "1";

        UUID userReceivingId = UserHelper.userId;
        UUID userSendingId = UUID.randomUUID();

        TradeOffer tradeOffer = new TradeOffer(
                cardToSendId,
                cardToReceiveId,
                userReceivingId,
                userSendingId,
                tradeOrder
        );

        return tradeOffer;
    }
}
