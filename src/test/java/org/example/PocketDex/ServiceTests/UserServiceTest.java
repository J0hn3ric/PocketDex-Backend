package org.example.PocketDex.ServiceTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cdimascio.dotenv.Dotenv;
import org.example.PocketDex.Model.User;
import org.example.PocketDex.Service.JWTService;
import org.example.PocketDex.Service.UserService;
import org.example.PocketDex.ServiceTests.helpers.UserServiceHelper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    private UUID userId;
    private String jwtToken;
    final String testEmail = "test-email@example.com";
    final String testPassword = "test-password";


    @BeforeAll
    static void loadEnv() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
    }

    @BeforeEach
    void signupTestUser() {
        JsonNode response = userService.signup(
                testEmail,
                testPassword
        ).block();

        userId = UUID.fromString(response.get("user").get("id").asText());
        jwtToken = "Bearer " + response.get("access_token").asText();
    }

    private void deleteAuthUser() {
        String url = System.getProperty("SUPABASE_URL");
        String apiKey = System.getProperty("SUPABASE_SECRET_API_KEY");

        WebClient
                .builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader("apikey", apiKey)
                .build()
                .delete()
                .uri("/auth/v1/admin/users/" + userId)
                .retrieve()
                .bodyToMono(Void.class)
                .block();

        System.out.println("deleted test users!!");
    }

    @Nested
    public class RestTests {
        @AfterEach
        void deleteTestUser() {
            String url = System.getProperty("SUPABASE_URL");
            String apiKey = System.getProperty("SUPABASE_SECRET_API_KEY");

            WebClient
                    .builder()
                    .baseUrl(url)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader("apikey", apiKey)
                    .build()
                    .delete()
                    .uri("/auth/v1/admin/users/" + userId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            System.out.println("deleted test users!!");
        }

        @Test
        void userService_AddUserProfileInformation_InsertedUserInformationInDB() {
            User userToAdd = new User(
                    userId.toString(),
                    "friendId",
                    "username",
                    "userImg"
            );

            try {
                userService.insertUserProfileInfo(
                        userToAdd.getFriendId(),
                        userToAdd.getUsername(),
                        userToAdd.getUserImg(),
                        jwtToken
                ).block();

                List<User> response = userService.getUserProfileInfoById(
                        jwtToken,
                        userId.toString()).block();

                assertAll(
                        () -> assertEquals(userToAdd.getUsername(), response.get(0).getUsername()),
                        () -> assertEquals(userToAdd.getFriendId(), response.get(0).getFriendId()),
                        () -> assertEquals(userToAdd.getUserImg(), response.get(0).getUserImg())
                );

                assertTrue(true);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                deleteTestUser();
                boolean failed = true;
                assertFalse(failed);
            }
        }

        @Test
        void userService_AddUserProfileInformationWithInvalidToken_ThrowsIllegalArgumentException() {
            String invalidToken = "invalidToken Bearer";

            User userToAdd = new User(
                    userId.toString(),
                    "friendId",
                    "username",
                    "userImg"
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.insertUserProfileInfo(
                            userToAdd.getFriendId(),
                            userToAdd.getUsername(),
                            userToAdd.getUserImg(),
                            invalidToken
                    ).block()
            );
        }

        @Test
        void userService_GetUserProfileInfoFromValidID_UserInformationQueriedAreCorrect() {
            String expectedId = userId.toString();
            String expectedUsername = "username";
            String expectedFriendId = "friendId";
            String expectedUserImg = "userImg";

            try {
                UserServiceHelper.insertNewUserProfileInformation(jwtToken, userService);

                List<User> userReturned = userService.getUserProfileInfoById(
                        jwtToken,
                        userId.toString()).block();

                assertAll(
                        () -> assertEquals(expectedId, userReturned.get(0).getId().toString()),
                        () -> assertEquals(expectedFriendId, userReturned.get(0).getFriendId()),
                        () -> assertEquals(expectedUserImg, userReturned.get(0).getUserImg()),
                        () -> assertEquals(expectedUsername, userReturned.get(0).getUsername())
                );
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                deleteTestUser();
                boolean failed = true;
                assertFalse(failed);
            }
        }

        @Test
        void userService_GetUserProfileInformationWithInvalidToken_ThrowsIllegalArgumentException() {
            String invalidToken = "invalidToken";

            assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.getUserProfileInfoById(invalidToken, userId.toString())
                            .block()
            );
        }

        @Test
        void userService_getUserProfileInformationFromNonExistingUserID_ReturnsEmptyList() {
            UUID nonExistingUserId = UUID.randomUUID();

            try{
                List<User> response = userService.getUserProfileInfoById(jwtToken, nonExistingUserId.toString())
                        .block();

                assertEquals(0, response.size());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                deleteTestUser();
                boolean failed = true;
                assertFalse(failed);
            }
        }

        @Test
        void userService_GetUserProfileInformationFromInvalidUsername_ReturnsEmptyJSONNode() {
            String invalidUsername = "invalid";

            try {
                UserServiceHelper.insertNewUserProfileInformation(
                        jwtToken,
                        userService
                );

                List<User> response = userService.getUserProfileInfoByUsername(
                        jwtToken,
                        invalidUsername
                ).block();

                assertEquals(0, response.size());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                deleteTestUser();
                boolean failed = true;
                assertFalse(failed);
            }
        }

        @Test
        void userService_GetUserProfileInformationFromUsername_ListOfUserInformationAreQueriedCorrectly() {
            List<User> usersAddedToDb = new ArrayList<>(UserServiceHelper.insertSeveralUsersToTable(userService));
            String usernameToSearch = "sameUsername";
            List<User> expectedUsersReturned = List.of(
                    usersAddedToDb.get(0),
                    usersAddedToDb.get(2)
            );
            try {
                List<User> usersReturned = userService.getUserProfileInfoByUsername(jwtToken, usernameToSearch).block();

                assertEquals(expectedUsersReturned.size(), usersReturned.size());

                for (int i = 0; i < usersReturned.size(); i++) {
                    int finalI = i;
                    assertAll(
                            () -> assertEquals(
                                    expectedUsersReturned.get(finalI).getUserImg(),
                                    usersReturned.get(finalI).getUserImg()
                            ),
                            () -> assertEquals(
                                    expectedUsersReturned.get(finalI).getUsername(),
                                    usersReturned.get(finalI).getUsername()
                            ),
                            () -> assertEquals(
                                    expectedUsersReturned.get(finalI).getFriendId(),
                                    usersReturned.get(finalI).getFriendId()
                            ),
                            () -> assertNotEquals(
                                    usersAddedToDb.get(1).getUsername(),
                                    usersReturned.get(finalI).getUsername()
                            )
                    );
                }

                UserServiceHelper.deleteSeveralUsersFromTable(usersAddedToDb);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                UserServiceHelper.deleteSeveralUsersFromTable(usersAddedToDb);
                deleteTestUser();
                boolean failed = true;
                assertFalse(failed);
            }
        }

        @Test
        void userService_UpdateUsernameWithValidUsername_UsernameUpdatedCorrectly() {
            String usernameBefore = "username";
            String expectedUserNameAfterChange = "newUsername";

            try {
                UserServiceHelper.insertNewUserProfileInformation(jwtToken, userService);

                List<User> userBefore = userService
                        .getUserProfileInfoById(
                                jwtToken,
                                userId.toString()
                        ).block();

                assertEquals(usernameBefore, userBefore.get(0).getUsername());

                ObjectMapper objectMapper = new ObjectMapper();

                ObjectNode payload = objectMapper.createObjectNode();

                payload.put("username", expectedUserNameAfterChange);

                System.out.println(payload.toString());

                userService.updateUserProfile(
                        jwtToken,
                        payload.get("username").asText(),
                        null
                ).block();

                List<User> userAfter = userService
                        .getUserProfileInfoById(
                                jwtToken,
                                userId.toString()
                        ).block();

                assertEquals(expectedUserNameAfterChange, userAfter.get(0).getUsername());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                deleteTestUser();
                boolean failed = true;
                assertFalse(failed);
            }
        }

        @Test
        void userService_UpdateUserImgWithValidUserImg_UserImgUpdatedCorrectly() {
            String userImgBefore = "userImg";
            String expectedUserImgAfterChange = "newUserImg";

            try {
                UserServiceHelper.insertNewUserProfileInformation(jwtToken, userService);

                List<User> userBefore = userService
                        .getUserProfileInfoById(
                                jwtToken,
                                userId.toString()
                        ).block();

                assertEquals(userImgBefore, userBefore.get(0).getUserImg());

                ObjectMapper objectMapper = new ObjectMapper();

                ObjectNode payload = objectMapper.createObjectNode();

                payload.put("user_img", expectedUserImgAfterChange);

                System.out.println(payload.toString());

                userService.updateUserProfile(
                        jwtToken,
                        null,
                        payload.get("user_img").asText()
                ).block();

                List<User> userAfter = userService
                        .getUserProfileInfoById(
                                jwtToken,
                                userId.toString()
                        ).block();

                assertEquals(expectedUserImgAfterChange, userAfter.get(0).getUserImg());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                deleteTestUser();
                boolean failed = true;
                assertFalse(failed);
            }
        }

        @Test
        void userService_UpdateUserProfileInformationWithValidInformation_UserProfileUpdatedCorrectly() {
            String usernameBefore = "username";
            String userImgBefore = "userImg";
            String expectedUserNameAfterChange = "newUsername";
            String expectedUserImgAfterChange = "newUserImg";

            try {
                UserServiceHelper.insertNewUserProfileInformation(jwtToken, userService);

                List<User> userBefore = userService
                        .getUserProfileInfoById(
                                jwtToken,
                                userId.toString()
                        ).block();


                    assertEquals(usernameBefore, userBefore.get(0).getUsername());
                    assertEquals(userImgBefore, userBefore.get(0).getUserImg());


                ObjectMapper objectMapper = new ObjectMapper();

                ObjectNode payload = objectMapper.createObjectNode();

                payload.put("username", expectedUserNameAfterChange);
                payload.put("user_img", expectedUserImgAfterChange);

                System.out.println(payload.toString());

                List<User> updatedRow = userService.updateUserProfile(
                        jwtToken,
                        payload.get("username").asText(),
                        payload.get("user_img").asText()
                ).block();

                assertEquals(expectedUserImgAfterChange, updatedRow.get(0).getUserImg());
                assertEquals(expectedUserNameAfterChange, updatedRow.get(0).getUsername());
                assertEquals(userId.toString(), updatedRow.get(0).getId().toString());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                deleteTestUser();
                boolean failed = true;
                assertFalse(failed);
            }
        }

        @Test
        void userService_UpdateUserProfileInformationWithInvalidToken_ThrowsIllegalArgumentException() {
            String invalidToken = "invalidToken";

            String newUsername = "newUsername";
            String newUserImg = "newUserImg";

            assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.updateUserProfile(
                            invalidToken,
                            newUsername,
                            newUserImg
                    ).block()
            );
        }



        @Test
        void userService_UpdateUserProfileInformationWithEmptyPayload_ThrowsIllegalArgumentException() {
            try {
                UserServiceHelper.insertNewUserProfileInformation(jwtToken, userService);

                ObjectMapper objectMapper = new ObjectMapper();

                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.updateUserProfile(
                                jwtToken,
                                null,
                                null
                        )
                );
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                deleteTestUser();
                boolean failed = true;
                assertFalse(failed);
            }
        }

    }



    @Test
    void userService_DeleteAuthUserWithValidToken_UserDeleted() {
        try {
            UserServiceHelper.insertNewUserProfileInformation(
                    jwtToken,
                    userService
            );

            System.out.println(userId.toString());

            userService.deleteAuthUser(
                    jwtToken
            ).block();

            List<User> returnedUser = userService.getUserProfileInfoById(
                    jwtToken,
                    userId.toString()
            ).block();

            assertEquals(0, returnedUser.size());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            deleteAuthUser();
            boolean failed = true;
            assertFalse(failed);
        }
    }

    @Test
    void userService_DeleteAuthUserWithInvalidToken_ThrowsIllegalArgumentException() {
        String invalidToken = "invalidToken";

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteAuthUser(invalidToken).block()
        );

        deleteAuthUser();
    }
}
