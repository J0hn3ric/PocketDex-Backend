package org.example.PocketDex.Model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
public class UserCard{

    @JsonProperty("card_id")
    private String cardId;

    @JsonProperty("quantity")
    private int quantity = 1;

    @JsonProperty("user_id")
    private UUID userId;

    public UserCard(
            String cardId,
            UUID userId,
            int quantity
    ) {
        this.cardId = cardId;
        this.userId = userId;
        this.quantity = quantity;
    }

    public UserCard(
            String cardId,
            int quantity
    ) {
        this.cardId = cardId;
        this.quantity = quantity;
    }

    @JsonIgnore
    public Boolean isTradable() {
        return this.quantity >= 2;
    }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }
}
