package org.example.PocketDex.Model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "packs")
@Getter
@Setter
@NoArgsConstructor
public class Pack {
    @Id
    private String id;

    @Field("name")
    private String packName;

    @Field("res")
    private String packRes;

    @Field("expansion")
    private String expansion;

    @Field("expansion_id")
    private String expansionId;

    public Pack(String id, String packName, String packRes, String expansion, String expansionId) {
        this.id = id;
        this.packName = packName;
        this.packRes = packRes;
        this.expansion = expansion;
        this.expansionId = expansionId;
    }
}
