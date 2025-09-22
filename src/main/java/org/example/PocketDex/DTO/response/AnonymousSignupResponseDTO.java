package org.example.PocketDex.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class AnonymousSignupResponse {
    private String token;

    public AnonymousSignupResponse(
            String token
    ) {
        this.token = token;
    }
}
