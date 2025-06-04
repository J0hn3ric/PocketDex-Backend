package org.example.PocketDex.Model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UserCollection {
    private UUID userId;
    private List<UserCard> userCardsToUpdate = new ArrayList<>();
    private List<UserCard> userCardsToRemove = new ArrayList<>();

    // may need Adapter for Collection cardList
    public UserCollection(UUID userId, List<UserCard> cardList) {
        this.userId = userId;
        this.userCardsToUpdate = this.parseUserCardsToUpdate(cardList);
        this.userCardsToRemove = this.parseUserCardsToRemove(cardList);
    }

    public UserCollection(UUID userId) {
        this.userId = userId;
    }

    private ArrayList<UserCard> parseUserCardsToUpdate(List<UserCard> userCards) {
        ArrayList<UserCard> userCardsToUpdate = new ArrayList<>();

        userCards.forEach(userCard -> {
                if (userCard.getQuantity() > 0) {
                    userCardsToUpdate.add(userCard);
                }
            }
        );

        return userCardsToUpdate;
    }

    private ArrayList<UserCard> parseUserCardsToRemove(List<UserCard> userCards) {
        ArrayList<UserCard> userCardsToRemove = new ArrayList<>();

        userCards.forEach(userCard -> {
                    if (userCard.getQuantity() <= 0) {
                        userCardsToRemove.add(userCard);
                    }
                }
        );

        return userCardsToRemove;
    }
}
