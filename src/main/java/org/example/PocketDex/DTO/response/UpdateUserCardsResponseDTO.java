package org.example.PocketDex.DTO.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.PocketDex.Model.UserCard;

import java.util.List;


@Data
public class UpdateUserCardsResponse {
    private List<UserCard> updatedUserCards;
    private List<UserCard> deletedUserCards;

    public UpdateUserCardsResponse(
            List<UserCard> updatedUserCards,
            List<UserCard> deletedUserCards
    ) {
        this.updatedUserCards = updatedUserCards;
        this.deletedUserCards = deletedUserCards;
    }
}
