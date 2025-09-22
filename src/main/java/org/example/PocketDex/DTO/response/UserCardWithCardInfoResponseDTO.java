package org.example.PocketDex.DTO.response;

import lombok.Data;
import org.example.PocketDex.Model.Card;

@Data
public class UserCardWithCardInfo {
    private String userId;
    private int quantity;
    private boolean isTradable;
    private Card cardInfo;

    public UserCardWithCardInfo(
            String userId,

            int quantity,
            Card cardInfo
    ) {
        this.userId = userId;
        this.quantity = quantity;
        this.cardInfo = cardInfo;
    }
}

public record UserCardWithCardInfo
