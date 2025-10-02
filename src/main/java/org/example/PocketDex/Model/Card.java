package org.example.PocketDex.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.example.PocketDex.Rarity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "cards")
@Getter
@Setter
@NoArgsConstructor
public class Card {
    @Id
    private String id;

    @JsonProperty("card_pack")
    @Field("card_pack")
    private String packId;

    @JsonProperty("card_img")
    @Field("card_img")
    private String res;

    @JsonProperty("card_name")
    @Field("card_name")
    private String name;

    @JsonProperty("card_rarity")
    @Field("card_rarity")
    private Rarity rarity;

    @JsonProperty("expansion")
    @Field("expansion")
    private String expansion;

    public Card(String id, String res, String name, String rarity, String packId, String expansion) throws Exception {
        try {
            this.id = id;
            this.res = res;
            this.name = name;
            this.rarity = Rarity.fromValue(rarity);
            this.packId = packId;
            this.expansion = expansion;
        } catch (Error e) {
            throw e;
        }
    }

    public Card(String id, String res, String name, Rarity rarity, String packId, String expansion) {
        this.id = id;
        this.res = res;
        this.name = name;
        this.rarity = rarity;
        this.packId = packId;
        this.expansion = expansion;
    }
}
