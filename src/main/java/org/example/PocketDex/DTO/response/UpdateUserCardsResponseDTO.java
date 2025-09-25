package org.example.PocketDex.DTO.response;

import lombok.Data;
import org.example.PocketDex.Model.UserCard;

import java.util.List;

public record UpdateUserCardsResponseDTO(
        List<String> updatedUserCards,
        List<String> deletedUserCards
) {}
