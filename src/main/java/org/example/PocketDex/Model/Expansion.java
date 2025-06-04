package org.example.PocketDex.Model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "expansions")
@Getter
@Setter
@NoArgsConstructor
public class Expansion {
    @Id
    String id;

    @Field("name")
    String name;

    @Field("res")
    String res;

    public Expansion(String id, String name, String res) {
        this.id = id;
        this. res = res;
        this.name = name;
    }
}
