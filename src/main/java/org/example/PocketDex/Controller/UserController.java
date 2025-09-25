package org.example.PocketDex.Controller;

import org.example.PocketDex.DTO.request.UpdateUserProfileRequest;
import org.example.PocketDex.DTO.response.ApiResponseDTO;
import org.example.PocketDex.DTO.response.ResponseBodyDTO;
import org.example.PocketDex.DTO.response.UpdateUserProfileResponseDTO;
import org.example.PocketDex.Model.User;
import org.example.PocketDex.Service.JWTService;
import org.example.PocketDex.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @PostMapping("/signup")
    public Mono<ResponseEntity<ApiResponseDTO<ResponseBodyDTO<Void>>>> signupUser(
            @RequestBody User userInfo
    ) {

        return userService.createNewUser(
                userInfo.getFriendId(),
                userInfo.getUsername(),
                userInfo.getUserImg())
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(
                        new ApiResponseDTO<>(body, null)
                ))
                .onErrorResume(e ->
                    Mono.just(ResponseEntity.badRequest().body(new ApiResponseDTO<>(null, e.getMessage())))
                );
    }

    @PatchMapping("/user-profile")
    public Mono<ResponseEntity<ApiResponseDTO<ResponseBodyDTO<List<UpdateUserProfileResponseDTO>>>>> updateUserProfileInfo(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateUserProfileRequest updateUserProfileRequest
    ) {
        String jwtToken = jwtService.extractToken(authHeader);

        return userService.updateUserInfo(
                jwtToken,
                updateUserProfileRequest.getNewUsername(),
                updateUserProfileRequest.getNewUserImg())
                .map(body -> ResponseEntity.ok().body(
                        new ApiResponseDTO<>(body, null)
                ))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.badRequest().body(new ApiResponseDTO<>(null, e.getMessage())))
                );

    }

    @DeleteMapping("/delete-user")
    public Mono<ResponseEntity<ApiResponseDTO<ResponseBodyDTO<String>>>> deleteUser(
            @RequestHeader("Authorization") String authHeader
    ) {
        String jwtToken = jwtService.extractToken(authHeader);

        return userService.deleteUserUsingBackendToken(jwtToken)
                .map(body -> ResponseEntity.ok().body(
                        new ApiResponseDTO<>(body, null)
                ))
                .onErrorResume( e ->
                        Mono.just(ResponseEntity.badRequest().body(new ApiResponseDTO<>(null, e.getMessage())))
                );
    }

    @GetMapping("/user-profile/by-id")
    public Mono<ResponseEntity<ApiResponseDTO<ResponseBodyDTO<List<User>>>>> getUserById(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String id
    ) {
        String jwtToken = jwtService.extractToken(authHeader);

        return userService.getUserInfoById(jwtToken, id)
                .map(body -> ResponseEntity.ok().body(
                        new ApiResponseDTO<>(body, null)
                ))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.badRequest().body(new ApiResponseDTO<>(null, e.getMessage())))
                );
    }

    @GetMapping("/user-profile/by-username")
    public Mono<ResponseEntity<ApiResponseDTO<ResponseBodyDTO<List<User>>>>> getUserByUsername(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String username
    ) {
        String jwtToken = jwtService.extractToken(authHeader);

        return userService.getUserInfoByUsername(jwtToken, username)
                .map(body -> ResponseEntity.ok().body(
                        new ApiResponseDTO<>(body, null)
                ))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.badRequest().body(new ApiResponseDTO<>(null, e.getMessage())))
                );
    }


}
