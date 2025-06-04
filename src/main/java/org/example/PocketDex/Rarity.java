package org.example.PocketDex;

import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum Rarity {
    ONE_DIA("1-Dia"),
    TWO_DIA("2-Dia"),
    THREE_DIA("3-Dia"),
    FOUR_DIA("4-Dia"),
    ONE_STAR("1-Star"),
    TWO_STAR("2-Star"),
    THREE_STAR("3-Star"),
    CROWN("Crown"),
    ONE_SHINY("1-Shiny"),
    TWO_SHINY("2-Shiny");

    private String rarityString;

    Rarity(String rarityString) {
        this.rarityString = rarityString;
    }

    public static Rarity fromValue(String codeString) {
        return Stream.of(Rarity.values())
                .filter(code -> codeString.equals(code.getRarityString()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
