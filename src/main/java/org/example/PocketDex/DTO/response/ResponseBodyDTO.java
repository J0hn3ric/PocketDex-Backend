package org.example.PocketDex.DTO.response;

import lombok.Data;

public record ResponseBodyDTO<T>(
        T data,
        String backendToken
) {}
