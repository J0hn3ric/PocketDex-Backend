package org.example.PocketDex.Controller.utils;

import org.example.PocketDex.DTO.response.ApiResponseDTO;
import org.example.PocketDex.Service.JWTService;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public class ControllerUtils {

    private ControllerUtils() {}

    public static <T> Mono<ResponseEntity<ApiResponseDTO<T>>> toApiResponse(Mono<T> mono) {
        return mono
                .map(body -> ResponseEntity.ok(new ApiResponseDTO<>(body, null)))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity
                                .badRequest()
                                .body(new ApiResponseDTO<>(null, e.getMessage())))
                );
    }

    public static String extractToken(String authHeader, JWTService jwtService) {
        return jwtService.extractToken(authHeader);
    }
}
