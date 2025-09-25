package org.example.PocketDex.ServiceTests;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.PocketDex.DTO.response.ResponseBodyDTO;
import org.example.PocketDex.DTO.response.UpdateUserProfileResponseDTO;
import org.example.PocketDex.Model.User;
import org.example.PocketDex.Service.JWTService;
import org.example.PocketDex.Service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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

    private String backendToken;


    @BeforeAll
    static void loadEnv() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
    }

    @AfterEach
    void deleteUser() {
        if (backendToken != null) {
            userService.deleteUserUsingBackendToken(backendToken).block();
        } else {
            System.out.println("no backend token set!!");
        }
    }

    @Test
    void userService_CreateNewUser_NewUserInstanceAddedToDbSuccessfully() {
        try {
            String expectedFriendId = "friend_id";
            String expectedUsername = "username";
            String expectedUserImg = "user_img";

            ResponseBodyDTO<Void> createUserResponse = userService.createNewUser(
                    expectedFriendId,
                    expectedUsername,
                    expectedUserImg
            ).block();

            assertNotNull(createUserResponse);

            backendToken = createUserResponse.backendToken();

            assertNotEquals("", backendToken);

            ResponseBodyDTO<List<User>> getUserByUsernameResponse = userService
                    .getUserInfoByUsername(backendToken, expectedUsername)
                    .block();

            assertNotNull(getUserByUsernameResponse);

            User userData = getUserByUsernameResponse.data().getFirst();

            assertAll(
                    () -> assertEquals(backendToken, getUserByUsernameResponse.backendToken()),
                    () -> assertEquals(expectedUsername, userData.getUsername()),
                    () -> assertEquals(expectedFriendId, userData.getFriendId()),
                    () -> assertEquals(expectedUserImg, userData.getUserImg()),
                    () -> assertInstanceOf(UUID.class, userData.getId())
            );
        } catch (Exception e) {
            deleteUser();
            System.out.println("Error: " + e.getMessage());
            assertFalse(true);
        }
    }

    @Test
    void userService_MakeRequestWithInvalidToken_ThrowsRuntimeException() {
        String invalidToken = "invalid";

        assertAll(
                () -> assertThrows(
                        RuntimeException.class,
                        () -> userService.updateUserInfo(
                                invalidToken,
                                "some_username",
                                "some_user_img"
                        ).block()
                ),
                () -> assertThrows(
                        RuntimeException.class,
                        () -> userService.getUserInfoById(
                                invalidToken,
                                "some_id"
                        ).block()
                ),
                () -> assertThrows(
                        RuntimeException.class,
                        () -> userService.getUserInfoByUsername(
                                invalidToken,
                                "some_username"
                        ).block()
                ),
                () -> assertThrows(
                        RuntimeException.class,
                        () -> userService.deleteUserUsingBackendToken(invalidToken).block()
                )
        );

    }

    @Test
    void userService_DeleteNonExistentUserWithValidToken_ThrowsSessionExpiredException() {
        try {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.deleteUserUsingBackendToken(backendToken)
            );
        } catch (Exception e) {
            deleteUser();
            System.out.println("Error: " + e.getMessage());
            assertFalse(true);
        }
    }

    @Nested
    public class UserInstanceAlreadyPresentInDB {

        private UUID userId;
        final String testFriendId = "friend_id";
        final String testUsername = "username";
        final String testUserImg = "user_img";

        @BeforeEach
        void createUserInstanceInDB() {
            ResponseBodyDTO<Void> createUserResponse = userService.createNewUser(
                    testFriendId,
                    testUsername,
                    testUserImg
            ).block();

            assertNotNull(createUserResponse);

            backendToken = createUserResponse.backendToken();

            User user = userService
                    .getUserInfoByUsername(
                            backendToken,
                            testUsername
                    ).block()
                    .data()
                    .getFirst();

            userId = user.getId();
        }

        @Test
        void userService_CreateNewUserWithDuplicateFriendId_ThrowsException() {
            try {
                String newUsername = "new_username";
                String newUserImg = "new_user_img";

                assertThrows(
                        RuntimeException.class,
                        () -> userService.createNewUser(
                                testFriendId,
                                newUsername,
                                newUserImg
                        ).block()
                );
            } catch (Exception e) {
                deleteUser();
                System.out.println("Error: " + e.getMessage());
                assertFalse(true);
            }
        }

        @Test
        void  userService_GetUserInfoWithValidId_UserInfoQueriedIsCorrect() {
            try {
                String expectedFriendId = testFriendId;
                String expectedUsername = testUsername;
                String expectedUserImg = testUserImg;

                assertNotNull(userId);

                ResponseBodyDTO<List<User>> getUserByIdResponse = userService
                        .getUserInfoById(
                            backendToken,
                            String.valueOf(userId)
                        ).block();

                assertNotNull(getUserByIdResponse);
                assertEquals(backendToken, getUserByIdResponse.backendToken());

                User userReturned = getUserByIdResponse
                        .data()
                        .getFirst();

                assertAll(
                        () -> assertEquals(expectedUsername, userReturned.getUsername()),
                        () -> assertEquals(expectedFriendId, userReturned.getFriendId()),
                        () -> assertEquals(expectedUserImg, userReturned.getUserImg())
                );
            } catch (Exception e) {
                deleteUser();
                System.out.println("Error: " + e.getMessage());
                assertFalse(true);
            }
        }

        @Test
        void userService_GetUserInfoWithValidUsername_UserInfoQueriedCorrectly() {
            try {
                String expectedFriendId = testFriendId;
                String expectedUsername = testUsername;
                String expectedUserImg = testUserImg;

                ResponseBodyDTO<List<User>> getUserByUsernameResponse = userService
                        .getUserInfoByUsername(
                                backendToken,
                                testUsername
                        ).block();

                assertNotNull(getUserByUsernameResponse);
                assertEquals(backendToken, getUserByUsernameResponse.backendToken());

                User userReturned = getUserByUsernameResponse
                        .data()
                        .getFirst();

                assertAll(
                        () -> assertEquals(expectedUsername, userReturned.getUsername()),
                        () -> assertEquals(expectedFriendId, userReturned.getFriendId()),
                        () -> assertEquals(expectedUserImg, userReturned.getUserImg())
                );
            } catch (Exception e) {
                deleteUser();
                System.out.println("Error: " + e.getMessage());
                assertFalse(true);
            }
        }

        @Test
        void userService_UpdateUserInfoWithValidUsernameAndUserImg_UserInfoUpdatedCorrectly() {
            try {
                String newUsername = "new_username";
                String newUserImg = "new_user_img";
                String expectedUsername = newUsername;
                String expectedUserImg = newUserImg;
                String expectedFriendId = testFriendId;

                ResponseBodyDTO<List<UpdateUserProfileResponseDTO>> updateUserInfoResponse = userService
                        .updateUserInfo(backendToken, newUsername, newUserImg)
                        .block();

                assertNotNull(updateUserInfoResponse);
                assertEquals(backendToken, updateUserInfoResponse.backendToken());

                ResponseBodyDTO<List<User>> getUserInfoResponse = userService
                        .getUserInfoById(backendToken, userId.toString())
                        .block();

                assertNotNull(getUserInfoResponse);
                assertEquals(backendToken, getUserInfoResponse.backendToken());

                User userInfoReturned = getUserInfoResponse
                        .data()
                        .getFirst();

                assertAll(
                        () -> assertEquals(expectedUsername, userInfoReturned.getUsername()),
                        () -> assertEquals(expectedUserImg, userInfoReturned.getUserImg())
                );
            } catch (Exception e) {
                deleteUser();
                System.out.println("Error: " + e.getMessage());
                assertFalse(true);
            }
        }

        @Test
        void userService_UpdateUserInfoWithValidUsername_UserInfoUpdatedCorrectly() {
            try {
                String newUsername = "new_username";
                String expectedUsername = newUsername;
                String expectedUserImg = testUserImg;
                String expectedFriendId = testFriendId;

                ResponseBodyDTO<List<UpdateUserProfileResponseDTO>> updateUserInfoResponse = userService
                        .updateUserInfo(backendToken, newUsername, null)
                        .block();

                assertNotNull(updateUserInfoResponse);
                assertEquals(backendToken, updateUserInfoResponse.backendToken());

                ResponseBodyDTO<List<User>> getUserInfoResponse = userService
                        .getUserInfoById(backendToken, userId.toString())
                        .block();

                assertNotNull(getUserInfoResponse);
                assertEquals(backendToken, getUserInfoResponse.backendToken());

                User userInfoReturned = getUserInfoResponse
                        .data()
                        .getFirst();

                assertAll(
                        () -> assertEquals(expectedUsername, userInfoReturned.getUsername()),
                        () -> assertEquals(expectedUserImg, userInfoReturned.getUserImg()),
                        () -> assertEquals(expectedFriendId, userInfoReturned.getFriendId())
                );
            } catch (Exception e) {
                deleteUser();
                System.out.println("Error: " + e.getMessage());
                assertFalse(true);
            }
        }

        @Test
        void userService_UpdateUserInfoWithValidUserImg_UserInfoUpdatedCorrectly() {
            try {
                String newUserImg = "new_user_img";
                String expectedUsername = testUsername;
                String expectedUserImg = newUserImg;
                String expectedFriendId = testFriendId;

                ResponseBodyDTO<List<UpdateUserProfileResponseDTO>> updateUserInfoResponse = userService
                        .updateUserInfo(backendToken, null, newUserImg)
                        .block();

                assertNotNull(updateUserInfoResponse);
                assertEquals(backendToken, updateUserInfoResponse.backendToken());

                ResponseBodyDTO<List<User>> getUserInfoResponse = userService
                        .getUserInfoById(backendToken, userId.toString())
                        .block();

                assertNotNull(getUserInfoResponse);
                assertEquals(backendToken, getUserInfoResponse.backendToken());

                User userInfoReturned = getUserInfoResponse
                        .data()
                        .getFirst();

                assertAll(
                        () -> assertEquals(expectedUsername, userInfoReturned.getUsername()),
                        () -> assertEquals(expectedUserImg, userInfoReturned.getUserImg()),
                        () -> assertEquals(expectedFriendId, userInfoReturned.getFriendId())
                );
            } catch (Exception e) {
                deleteUser();
                System.out.println("Error: " + e.getMessage());
                assertFalse(true);
            }
        }

        @Test
        void userService_GetUserInfoWithNonExistentId_ReturnsEmptyList() {
            try {
                String nonExistentId = UUID.randomUUID().toString();
                int expectedUserListSize = 0;

                ResponseBodyDTO<List<User>> getUserByIdResponse = userService
                        .getUserInfoById(backendToken, nonExistentId)
                        .block();

                assertNotNull(getUserByIdResponse);

                List<User> userList = getUserByIdResponse.data();

                assertAll(
                        () -> assertEquals(backendToken, getUserByIdResponse.backendToken()),
                        () -> assertEquals(expectedUserListSize, userList.size())
                );
            } catch (Exception e) {
                deleteUser();
                System.out.println("Error: " + e.getMessage());
                assertFalse(true);
            }
        }

        @Test
        void userService_getUserInfoWithNonExistentUsername_ReturnsEmptyList() {
            try {
                String nonExistentUsername = "i_dont_exist";
                int expectedUserListSize = 0;

                ResponseBodyDTO<List<User>> getUserByUsernameResponse = userService
                        .getUserInfoByUsername(backendToken, nonExistentUsername)
                        .block();

                assertNotNull(getUserByUsernameResponse);

                List<User> userList = getUserByUsernameResponse.data();

                assertAll(
                        () -> assertEquals(backendToken, getUserByUsernameResponse.backendToken()),
                        () -> assertEquals(expectedUserListSize, userList.size())
                );
            } catch (Exception e) {
                deleteUser();
                System.out.println("Error: " + e.getMessage());
                assertFalse(true);
            }
        }

        @Test
        void userService_UpdateUserInfoWithInvalidInfo_ThrowsIllegalArgumentException() {
            try {
                assertAll(
                        () -> assertThrows(
                                IllegalArgumentException.class,
                                () -> userService.updateUserInfo(backendToken, null, null)
                                        .block()
                        ),
                        () -> assertThrows(
                                IllegalArgumentException.class,
                                () -> userService.updateUserInfo(backendToken, "", "")
                                        .block()
                        ),
                        () -> assertThrows(
                                IllegalArgumentException.class,
                                () -> userService.updateUserInfo(backendToken, null, "")
                                        .block()
                        ),
                        () -> assertThrows(
                                IllegalArgumentException.class,
                                () -> userService.updateUserInfo(backendToken, "", null)
                                        .block()
                        )
                );
            } catch (Exception e) {
                deleteUser();
                System.out.println("Error: " + e.getMessage());
                assertFalse(true);
            }
        }

    }


    /*
    @BeforeEach
    void signupTestUser() {
        JsonNode response = userService.signup(
                testEmail,
                testPassword
        ).block();

        userId = UUID.fromString(response.get("user").get("id").asText());
        jwtToken = "Bearer " + response.get("access_token").asText();
    }

    private void deleteUserUsingBackendToken() {
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

                List<User> response = userService.getUserInfoById(
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

                List<User> userReturned = userService.getUserInfoById(
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
                    () -> userService.getUserInfoById(invalidToken, userId.toString())
                            .block()
            );
        }

        @Test
        void userService_getUserProfileInformationFromNonExistingUserID_ReturnsEmptyList() {
            UUID nonExistingUserId = UUID.randomUUID();

            try{
                List<User> response = userService.getUserInfoById(jwtToken, nonExistingUserId.toString())
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

                List<User> response = userService.getUserInfoByUsername(
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
                List<User> usersReturned = userService.getUserInfoByUsername(jwtToken, usernameToSearch).block();

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
                        .getUserInfoById(
                                jwtToken,
                                userId.toString()
                        ).block();

                assertEquals(usernameBefore, userBefore.get(0).getUsername());

                ObjectMapper objectMapper = new ObjectMapper();

                ObjectNode payload = objectMapper.createObjectNode();

                payload.put("username", expectedUserNameAfterChange);

                System.out.println(payload.toString());

                userService.updateUserInfo(
                        jwtToken,
                        payload.get("username").asText(),
                        null
                ).block();

                List<User> userAfter = userService
                        .getUserInfoById(
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
                        .getUserInfoById(
                                jwtToken,
                                userId.toString()
                        ).block();

                assertEquals(userImgBefore, userBefore.get(0).getUserImg());

                ObjectMapper objectMapper = new ObjectMapper();

                ObjectNode payload = objectMapper.createObjectNode();

                payload.put("user_img", expectedUserImgAfterChange);

                System.out.println(payload.toString());

                userService.updateUserInfo(
                        jwtToken,
                        null,
                        payload.get("user_img").asText()
                ).block();

                List<User> userAfter = userService
                        .getUserInfoById(
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
                        .getUserInfoById(
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

                List<User> updatedRow = userService.updateUserInfo(
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
                    () -> userService.updateUserInfo(
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
                        () -> userService.updateUserInfo(
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

            userService.deleteUserUsingBackendToken(
                    jwtToken
            ).block();

            List<User> returnedUser = userService.getUserInfoById(
                    jwtToken,
                    userId.toString()
            ).block();

            assertEquals(0, returnedUser.size());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            deleteUserUsingBackendToken();
            boolean failed = true;
            assertFalse(failed);
        }
    }

    @Test
    void userService_DeleteAuthUserWithInvalidToken_ThrowsIllegalArgumentException() {
        String invalidToken = "invalidToken";

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteUserUsingBackendToken(invalidToken).block()
        );

        deleteUserUsingBackendToken();
    }

    */
}
