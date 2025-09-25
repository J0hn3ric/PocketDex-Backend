package org.example.PocketDex.ServiceTests.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.PocketDex.DTO.response.ResponseBodyDTO;
import org.example.PocketDex.Model.User;
import org.example.PocketDex.Service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;


public class UserServiceHelper {

    public static ResponseBodyDTO<List<User>> createNewUser(UserService userService) {
        final String testFriendId = "friend_id";
        final String testUsername = "username";
        final String testUserImg = "user_img";

        ResponseBodyDTO<Void> createUserResponse = userService.createNewUser(
                testFriendId,
                testUsername,
                testUserImg
        ).block();

        assertNotNull(createUserResponse);

        String backendToken = createUserResponse.backendToken();

        return userService
                .getUserInfoByUsername(
                        backendToken,
                        testUsername
                ).block();
    }
}
