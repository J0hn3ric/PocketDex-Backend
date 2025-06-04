package org.example.PocketDex.Model;

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

    @Field("card_pack")
    private String packId;

    @Field("card_img")
    private String res;

    @Field("card_name")
    private String name;

    @Field("card_rarity")
    private Rarity rarity;

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
