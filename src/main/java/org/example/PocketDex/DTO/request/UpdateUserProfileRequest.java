package org.example.PocketDex.DTO.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserProfileRequest {
    @JsonProperty("username")
    private String newUsername;
    @JsonProperty("user_img")
    private String newUserImg;

    public UpdateUserProfileRequest(String newUsername ,String newUserImg) {
        this.newUsername = newUsername;
        this.newUserImg = newUserImg;
    }
}
