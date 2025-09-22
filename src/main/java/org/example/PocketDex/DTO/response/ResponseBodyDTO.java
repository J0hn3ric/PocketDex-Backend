package org.example.PocketDex.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ResponseBody<T> {
    private T data;
    private String backendToken;

    public ResponseBody(
            T data,
            String backendToken
    ) {
        this.data = data;
        this.backendToken = backendToken;
    }

}
