package org.example.PocketDex.Controller;

import org.example.PocketDex.Controller.utils.ControllerUtils;
import org.example.PocketDex.DTO.response.ApiResponseDTO;
import org.example.PocketDex.DTO.response.ResponseBodyDTO;
import org.example.PocketDex.DTO.response.UpdateUserCardsResponseDTO;
import org.example.PocketDex.DTO.response.UserCardWithCardInfoResponseDTO;
import org.example.PocketDex.Model.UserCard;
import org.example.PocketDex.Service.JWTService;
import org.example.PocketDex.Service.UserCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/user-cards")
public class UserCardController {

    @Autowired
    private UserCardService userCardService;

    @Autowired
    private JWTService jwtService;

    @PostMapping("/me")
    public Mono<ResponseEntity<ApiResponseDTO<ResponseBodyDTO<UpdateUserCardsResponseDTO>>>>
    bulkUpdateUserCards(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody List<UserCard> userCardsToUpdate
    ) {
        String backendToken = ControllerUtils.extractToken(authHeader, jwtService);

        return ControllerUtils.toApiResponse(
                userCardService.updateUserCards(backendToken, userCardsToUpdate)
        );
    }

    @PostMapping("/me/new")
    public Mono<ResponseEntity<ApiResponseDTO<ResponseBodyDTO<UpdateUserCardsResponseDTO>>>>
    addNewCards(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody List<UserCard> userCardsToAdd
    ) {
        String backendToken = ControllerUtils.extractToken(authHeader, jwtService);

        return ControllerUtils.toApiResponse(
                userCardService.addNewUserCards(backendToken, userCardsToAdd)
        );
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<ApiResponseDTO<ResponseBodyDTO<List<UserCardWithCardInfoResponseDTO>>>>>
    getOwnedUserCards(
            @RequestHeader("Authorization") String authHeader
    ) {
        String backendToken = ControllerUtils.extractToken(authHeader, jwtService);

        return ControllerUtils.toApiResponse(
                userCardService.getOwnedUserCards(backendToken)
        );
    }

    @GetMapping("/user/{id}")
    public Mono<ResponseEntity<ApiResponseDTO<ResponseBodyDTO<List<UserCardWithCardInfoResponseDTO>>>>>
    getUserCardsByUserId(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") String userId
    ) {
        String backendToken = ControllerUtils.extractToken(authHeader, jwtService);

        return ControllerUtils.toApiResponse(
                userCardService.getUserCardsByUserId(backendToken, userId)
        );
    }

    @GetMapping("/me/cards/{id}")
    public Mono<ResponseEntity<ApiResponseDTO<ResponseBodyDTO<List<UserCard>>>>>
    getSingleUserCard(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") String cardId
    ) {
        String backendToken = ControllerUtils.extractToken(authHeader, jwtService);

        return ControllerUtils.toApiResponse(
                userCardService.getUserCardByCardId(backendToken, cardId)
        );
    }

}
