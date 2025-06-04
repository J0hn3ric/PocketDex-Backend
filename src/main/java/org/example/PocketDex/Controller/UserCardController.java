package org.example.PocketDex.Controller;

import org.example.PocketDex.DTO.response.UpdateUserCardsResponse;
import org.example.PocketDex.Model.User;
import org.example.PocketDex.Model.UserCard;
import org.example.PocketDex.Service.UserCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/user-cards")
public class UserCardController {

    @Autowired
    private UserCardService userCardService;

    @PostMapping
    public Mono<ResponseEntity<UpdateUserCardsResponse>> updateUserCards(
            @RequestHeader("Authorization") String jwtToken,
            @RequestBody List<UserCard> userCardsToUpdate
            ) {
        return userCardService.updateUserCards(jwtToken, userCardsToUpdate)
                .map(response ->
                        ResponseEntity.status(HttpStatus.OK).body(response));
    }
/*
    @GetMapping("/user")
    public Mono<ResponseEntity<?>> getUserCards(
            @RequestHeader("Authorization") String jwtToken,
            @RequestParam() String userId
    ) {


    }
 */

}
