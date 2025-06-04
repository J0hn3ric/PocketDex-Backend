package org.example.PocketDex.DTO.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserCardsResponse {
    private String status;
    private String message;

    public UpdateUserCardsResponse(
            String status,
            String message
    ) {
        this.status = status;
        this.message = message;
    }
}
