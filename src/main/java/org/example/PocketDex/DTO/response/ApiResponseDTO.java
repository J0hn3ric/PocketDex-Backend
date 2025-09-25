package org.example.PocketDex.DTO.response;

import lombok.Data;

public record ApiResponseDTO<T>(
        T body,
        String error
) {}
