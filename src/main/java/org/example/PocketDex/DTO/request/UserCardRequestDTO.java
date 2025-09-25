package org.example.PocketDex.DTO.request;

import lombok.Data;
import org.example.PocketDex.Model.UserCard;

import java.util.UUID;

@Data
public class UserCardRequestDTO {

    private int quantity;
    private String cardId;

    public UserCardRequestDTO(String cardId, int quantity) {
        this.quantity = quantity;
        this.cardId = cardId;
    }

    public UserCard toUserCard(UUID userId) {
        return new UserCard(
                cardId,
                userId,
                quantity
        );
    }
}
