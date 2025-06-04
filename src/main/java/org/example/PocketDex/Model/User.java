package org.example.PocketDex.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
public class User {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("friend_id")
    private String friendId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("user_img")
    private String userImg;

    // from db
    public User(
            String id,
            String friendId,
            String username,
            String userImg
    ) {
        this.id = UUID.fromString(id);
        this.friendId = friendId;
        this.username = username;
        this.userImg = userImg;
    }

    public User(
            String friendId,
            String username,
            String userImg
    ) {
        this.username = username;
        this.friendId = friendId;
        this.userImg = userImg;
    }
}
