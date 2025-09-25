package org.example.PocketDex.DTO.response;

import org.example.PocketDex.Model.Card;

public record UserCardWithCardInfoResponseDTO(
   int quantity,
   boolean isTradable,
   Card cardInfo
) {}
