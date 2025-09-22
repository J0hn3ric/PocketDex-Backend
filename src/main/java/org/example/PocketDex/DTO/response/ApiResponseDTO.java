package org.example.PocketDex.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ApiResponse<T> {
    private ResponseBody<T> body;
    private String error;

    public ApiResponse(
            ResponseBody<T> body,
            String error
    ) {
        this.body = body;
        this.error = error;
    }
}
