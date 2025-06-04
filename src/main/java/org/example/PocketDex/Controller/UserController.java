package org.example.PocketDex.Controller;

import org.example.PocketDex.DTO.request.SignUpRequest;
import org.example.PocketDex.DTO.request.UpdateUserProfileRequest;
import org.example.PocketDex.Model.User;
import org.example.PocketDex.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public Mono<ResponseEntity<Object>> userSignUp(
            @RequestBody SignUpRequest signupRequest
    ) {
        return userService.signup(signupRequest.getEmail(), signupRequest.getPassword())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body((Object) response))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }

    @PostMapping("/user-profile")
    public Mono<ResponseEntity<String>> insertUserProfileInfo(
            @RequestHeader("Authorization") String jwtToken,
            @RequestBody User insertUserProfileRequest
    ) {
        return userService.insertUserProfileInfo(
                insertUserProfileRequest.getFriendId(),
                insertUserProfileRequest.getUsername(),
                insertUserProfileRequest.getUserImg(),
                jwtToken)
                .then(Mono.just(ResponseEntity.status(HttpStatus.CREATED).body("user profile added succesfully!")))
                .onErrorResume(e ->
                    Mono.just(ResponseEntity.badRequest().body(e.getMessage()))
                );
    }

    @PatchMapping("/user-profile")
    public Mono<ResponseEntity<Object>> updateUserProfileInfo(
            @RequestHeader("Authorization") String jwtToken,
            @RequestBody UpdateUserProfileRequest updateUserProfileRequest
    ) {
        return userService.updateUserProfile(
                jwtToken,
                updateUserProfileRequest.getNewUsername(),
                updateUserProfileRequest.getNewUserImg())
                .map(response -> ResponseEntity.status(HttpStatus.OK).body((Object) response))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.badRequest().body(e.getMessage()))
                );

    }

    @DeleteMapping("/delete-user")
    public Mono<ResponseEntity<String>> deleteUser(
            @RequestHeader("Authorization") String jwtToken
    ) {
        return userService.deleteAuthUser(jwtToken)
                .then(Mono.just(ResponseEntity.status(HttpStatus.OK).body("user deleted successfully!")))
                .onErrorResume( e ->
                        Mono.just(ResponseEntity.badRequest().body(e.getMessage()))
                );
    }

    @GetMapping("/user-profile/by-id")
    public Mono<ResponseEntity<Object>> getUserById(
            @RequestHeader("Authorization") String jwtToken,
            @RequestParam String id
    ) {
        return userService.getUserProfileInfoById(jwtToken, id)
                .map(response -> ResponseEntity.status(HttpStatus.OK).body((Object) response))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.badRequest().body(e.getMessage()))
                );
    }

    @GetMapping("/user-profile/by-username")
    public Mono<ResponseEntity<Object>> getUserByUsername(
            @RequestHeader("Authorization") String jwtToken,
            @RequestParam String username
    ) {
        return userService.getUserProfileInfoByUsername(jwtToken, username)
                .map(response -> ResponseEntity.status(HttpStatus.OK).body((Object) response))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.badRequest().body(e.getMessage()))
                );
    }


}
