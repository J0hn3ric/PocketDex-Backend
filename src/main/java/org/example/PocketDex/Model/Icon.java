package org.example.PocketDex.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "icons")
@Getter
@Setter
@NoArgsConstructor
public class Icon {
    @Id
    private String id;

    @JsonProperty("img_url")
    @Field("img_url")
    private String iconImg;

    public Icon(String id, String iconImg) {
        this.id = id;
        this.iconImg = iconImg;
    }

}
