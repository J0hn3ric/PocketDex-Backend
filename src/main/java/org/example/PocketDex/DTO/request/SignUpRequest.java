package org.example.PocketDex.DTO.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    @JsonProperty("email")
    private String email;
    @JsonProperty("password")
    private String password;

    public SignUpRequest(
            String email,
            String password
    ) {
        this.email = email;
        this.password = password;
    }
}
