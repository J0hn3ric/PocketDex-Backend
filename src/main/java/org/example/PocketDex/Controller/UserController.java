package org.example.PocketDex.Controller;

import org.example.PocketDex.Controller.utils.ControllerUtils;
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

    @GetMapping("/me")
    public Mono<ResponseEntity<ApiResponseDTO<ResponseBodyDTO<User>>>> gewOwnInfo(
        @RequestHeader("Authorization") String authHeader
    ) {
        String backendToken = ControllerUtils.extractToken(authHeader, jwtService);

        return ControllerUtils.toApiResponse(
                userService.getOwnUserInfo(backendToken)
        );
    }


    @PostMapping("/signup")
    public Mono<ResponseEntity<ApiResponseDTO<ResponseBodyDTO<Void>>>> signupUser(
            @RequestBody User userInfo
    ) {
        return ControllerUtils.toApiResponse(
                userService.createNewUser(
                        userInfo.getFriendId(),
                        userInfo.getUsername(),
                        userInfo.getUserImg()
                )
        );
    }

    @PatchMapping("/me")
    public Mono<ResponseEntity<ApiResponseDTO<ResponseBodyDTO<UpdateUserProfileResponseDTO>>>> updateUserProfileInfo(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateUserProfileRequest updateUserProfileRequest
    ) {
        String backendToken = ControllerUtils.extractToken(authHeader, jwtService);

        return ControllerUtils.toApiResponse(
                userService.updateUserInfo(
                        backendToken,
                        updateUserProfileRequest.getNewUsername(),
                        updateUserProfileRequest.getNewUserImg()
                )
        );

    }

    @DeleteMapping("/me")
    public Mono<ResponseEntity<ApiResponseDTO<ResponseBodyDTO<String>>>> deleteUser(
            @RequestHeader("Authorization") String authHeader
    ) {
        String backendToken = ControllerUtils.extractToken(authHeader, jwtService);

        return ControllerUtils.toApiResponse(
                userService.deleteUserUsingBackendToken(backendToken)
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponseDTO<ResponseBodyDTO<User>>>> getUserById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") String id
    ) {
        String backendToken = ControllerUtils.extractToken(authHeader, jwtService);

        return ControllerUtils.toApiResponse(
                userService.getUserInfoById(backendToken, id)
        );
    }

    @GetMapping("/username/{username}")
    public Mono<ResponseEntity<ApiResponseDTO<ResponseBodyDTO<List<User>>>>> getUserByUsername(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("username") String username
    ) {
        String backendToken = ControllerUtils.extractToken(authHeader, jwtService);

        return ControllerUtils.toApiResponse(
                userService.getUserInfoByUsername(backendToken, username)
        );
    }


}
