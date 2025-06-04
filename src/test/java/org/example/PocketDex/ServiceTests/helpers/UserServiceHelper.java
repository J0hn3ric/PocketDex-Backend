package org.example.PocketDex.ServiceTests.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.PocketDex.Model.User;
import org.example.PocketDex.Service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserServiceHelper {

    public static void insertNewUserProfileInformation(String userKey, UserService userService) {


        userService.insertUserProfileInfo(
                "friendId",
                "username",
                "userImg",
                userKey
        ).block();
    }

    public static List<User> insertSeveralUsersToTable(UserService userService) {
        List<User> usersAddedToDB = new ArrayList<>();

        JsonNode response1 = userService
                .signup(
                        "test-email1@example.com",
                        "test-pasword1"
                ).block();

        JsonNode response2 = userService
                .signup(
                        "test-email2@example.com",
                        "test-password2"
                ).block();

        JsonNode response3 = userService
                .signup(
                        "test-email3@example.com",
                        "test-password3"
                ).block();

        UUID user1Id = UUID.fromString(response1.get("user").get("id").asText());
        String user1Key = "Bearer " + response1.get("access_token").asText();

        User user1 = new User(
                user1Id.toString(),
                "user1",
                "sameUsername",
                "user1"
        );

        usersAddedToDB.add(user1);

        UUID user2Id = UUID.fromString(response2.get("user").get("id").asText());
        String user2Key = "Bearer " + response2.get("access_token").asText();

        User user2 = new User(
                user2Id.toString(),
                "user2",
                "user2",
                "user2"
        );

        usersAddedToDB.add(user2);

        UUID user3Id = UUID.fromString(response3.get("user").get("id").asText());
        String user3Key = "Bearer " + response3.get("access_token").asText();

        User user3 = new User(
                user3Id.toString(),
                "user3",
                "sameUsername",
                "user3"
        );

        usersAddedToDB.add(user3);

        userService.insertUserProfileInfo(
                user1.getFriendId(),
                user1.getUsername(),
                user1.getFriendId(),
                user1Key
        ).block();
        userService.insertUserProfileInfo(
                user2.getFriendId(),
                user2.getUsername(),
                user2.getFriendId(),
                user2Key
        ).block();
        userService.insertUserProfileInfo(
                user3.getFriendId(),
                user3.getUsername(),
                user3.getFriendId(),
                user3Key
        ).block();

        return usersAddedToDB;
    }

    public static void deleteSeveralUsersFromTable(List<User> usersInTable) {
        usersInTable.forEach(u -> {
            String url = System.getProperty("SUPABASE_URL");
            String apiKey = System.getProperty("SUPABASE_SECRET_API_KEY");

            WebClient
                    .builder()
                    .baseUrl(url)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader("apikey", apiKey)
                    .build()
                    .delete()
                    .uri("/auth/v1/admin/users/" + u.getId().toString())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            System.out.println("deleted " + u.getUsername());
        });
    }
}
