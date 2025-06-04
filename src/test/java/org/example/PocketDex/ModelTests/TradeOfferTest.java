package org.example.PocketDex.ModelTests;

import org.example.PocketDex.Model.TradeOffer;
import org.example.PocketDex.Model.TradeOrder;
import org.example.PocketDex.ModelTests.helpers.TradeOfferHelper;
import org.example.PocketDex.ModelTests.helpers.UserHelper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TradeOfferTest {
    @Test
    public void tradeOffer_CrateInstanceFromDb_InstanceCreatedSuccessfully() {
        Long id = Long.valueOf(46532654665465L);
        boolean senderConfirmed = false;
        boolean receiverConfirmed = false;
        String cardToSendId = "1";
        String cardToReceiveId = "2";
        UUID userReceivingId = UserHelper.userId;
        UUID userSendingId = UUID.randomUUID();
        TradeOrder tradeOrder = new TradeOrder();

        TradeOffer tradeOffer = new TradeOffer(
                id,
                senderConfirmed,
                receiverConfirmed,
                cardToSendId,
                cardToReceiveId,
                userReceivingId,
                userSendingId,
                tradeOrder.getId()
        );

        assertAll(
                () -> assertEquals(id, tradeOffer.getId()),
                () -> assertEquals(senderConfirmed, tradeOffer.isSenderConfirmed()),
                () -> assertEquals(receiverConfirmed, tradeOffer.isReceiverConfirmed()),
                () -> assertEquals(cardToSendId, tradeOffer.getCardToSendId()),
                () -> assertEquals(cardToReceiveId, tradeOffer.getCardToReceiveId()),
                () -> assertEquals(userReceivingId, tradeOffer.getUserReceivingId()),
                () -> assertEquals(userSendingId, tradeOffer.getUserSendingId()),
                () -> assertEquals(tradeOrder.getId(), tradeOffer.getTradeOrderId())
        );
    }

    @Test
    public void tradeOffer_CreateNewInstance_InstanceCreatedSuccessfully() {
        String cardToSendId = "1";
        String cardToReceiveId = "2";
        UUID userReceivingId = UserHelper.userId;
        UUID usersendingId = UUID.randomUUID();
        TradeOrder tradeOrder = new TradeOrder();

        TradeOffer tradeOffer = new TradeOffer(
                cardToSendId,
                cardToReceiveId,
                userReceivingId,
                usersendingId,
                tradeOrder
        );

        assertAll(
                () -> assertNull(tradeOffer.getId()),
                () -> assertFalse(tradeOffer.isSenderConfirmed()),
                () -> assertFalse(tradeOffer.isReceiverConfirmed()),
                () -> assertEquals(cardToSendId, tradeOffer.getCardToSendId()),
                () -> assertEquals(cardToReceiveId, tradeOffer.getCardToReceiveId()),
                () -> assertEquals(userReceivingId, tradeOffer.getUserReceivingId()),
                () -> assertEquals(usersendingId, tradeOffer.getUserSendingId()),
                () -> assertEquals(tradeOrder.getId(), tradeOffer.getTradeOrderId())
        );
    }

    @Test
    public void tradeOffer_SenderConfirms_SenderConfirmedIsTrue() {
        TradeOffer tradeOffer = TradeOfferHelper.generateNewTradeOffer(new TradeOrder());

        assertFalse(tradeOffer.isSenderConfirmed());

        tradeOffer.senderConfirms();

        assertTrue(tradeOffer.isSenderConfirmed());
    }

    @Test
    public void tradeOffer_ReceiverConfirms_ReceiverConfirmedIsTrue() {
        TradeOffer tradeOffer = TradeOfferHelper.generateNewTradeOffer(new TradeOrder());

        assertFalse(tradeOffer.isReceiverConfirmed());

        tradeOffer.receiverConfirms();

        assertTrue(tradeOffer.isReceiverConfirmed());
    }

    @Test
    public void tradeOffer_SenderConfirmsWhenAlreadyConfirmed_NothingHappens() {
        TradeOffer tradeOffer = TradeOfferHelper.generateNewTradeOffer(new TradeOrder());
        tradeOffer.senderConfirms();
        boolean valueBefore = tradeOffer.isSenderConfirmed();

        assertTrue(valueBefore);
        tradeOffer.senderConfirms();
        assertTrue(tradeOffer.isSenderConfirmed());
    }

    @Test
    public void tradeOffer_ReceiverConfirmsWhenAlreadyConfirmed_NothingHappens() {
        TradeOffer tradeOffer = TradeOfferHelper.generateNewTradeOffer(new TradeOrder());
        tradeOffer.receiverConfirms();
        boolean valueBefore = tradeOffer.isReceiverConfirmed();

        assertTrue(valueBefore);
        tradeOffer.receiverConfirms();
        assertTrue(tradeOffer.isReceiverConfirmed());
    }

    @Test
    public void tradeOffer_OfferIsConfirmedByBothSides_OfferIsCompleted() {
        TradeOffer tradeOffer = TradeOfferHelper.generateNewTradeOffer(new TradeOrder());

        tradeOffer.receiverConfirms();
        tradeOffer.senderConfirms();

        assertTrue(tradeOffer.isCompleted());
    }

    @Test
    public void tradeOffer_OfferIsConfirmedByOnlyOneSide_OfferIsNotCompleted() {
        TradeOffer tradeOffer = TradeOfferHelper.generateNewTradeOffer(new TradeOrder());

        tradeOffer.senderConfirms();

        assertFalse(tradeOffer.isCompleted());
    }
}
