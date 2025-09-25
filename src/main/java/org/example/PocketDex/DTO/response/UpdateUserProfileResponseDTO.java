package org.example.PocketDex.DTO.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateUserProfileResponseDTO(
        @JsonProperty("username")
        String newUsername,
        @JsonProperty("user_img")
        String newUserImg
) {}
