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
        void userService_GetOwnUserInfo_UserInforReturnedCorrectly() {
            try {
                ResponseBodyDTO<User> response = userService.getOwnUserInfo(backendToken)
                        .block();

                assertNotNull(response);

                assertEquals(backendToken, response.backendToken());

                User userReturned = response
                        .data();

                assertAll(
                        () -> assertEquals(testUsername, userReturned.getUsername()),
                        () -> assertEquals(testFriendId, userReturned.getFriendId()),
                        () -> assertEquals(testUserImg, userReturned.getUserImg())
                );
            } catch (Exception e) {
                deleteUser();
                System.out.println("Error: " + e.getMessage());
                assertFalse(true);
            }
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

                ResponseBodyDTO<User> getUserByIdResponse = userService
                        .getUserInfoById(
                            backendToken,
                            String.valueOf(userId)
                        ).block();

                assertNotNull(getUserByIdResponse);
                assertEquals(backendToken, getUserByIdResponse.backendToken());

                User userReturned = getUserByIdResponse
                        .data();

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

                ResponseBodyDTO<UpdateUserProfileResponseDTO> updateUserInfoResponse = userService
                        .updateUserInfo(backendToken, newUsername, newUserImg)
                        .block();

                assertNotNull(updateUserInfoResponse);
                assertEquals(backendToken, updateUserInfoResponse.backendToken());

                ResponseBodyDTO<User> getUserInfoResponse = userService
                        .getUserInfoById(backendToken, userId.toString())
                        .block();

                assertNotNull(getUserInfoResponse);
                assertEquals(backendToken, getUserInfoResponse.backendToken());

                User userInfoReturned = getUserInfoResponse
                        .data();

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

                ResponseBodyDTO<UpdateUserProfileResponseDTO> updateUserInfoResponse = userService
                        .updateUserInfo(backendToken, newUsername, null)
                        .block();

                assertNotNull(updateUserInfoResponse);
                assertEquals(backendToken, updateUserInfoResponse.backendToken());

                ResponseBodyDTO<User> getUserInfoResponse = userService
                        .getUserInfoById(backendToken, userId.toString())
                        .block();

                assertNotNull(getUserInfoResponse);
                assertEquals(backendToken, getUserInfoResponse.backendToken());

                User userInfoReturned = getUserInfoResponse
                        .data();

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

                ResponseBodyDTO<UpdateUserProfileResponseDTO> updateUserInfoResponse = userService
                        .updateUserInfo(backendToken, null, newUserImg)
                        .block();

                assertNotNull(updateUserInfoResponse);
                assertEquals(backendToken, updateUserInfoResponse.backendToken());

                ResponseBodyDTO<User> getUserInfoResponse = userService
                        .getUserInfoById(backendToken, userId.toString())
                        .block();

                assertNotNull(getUserInfoResponse);
                assertEquals(backendToken, getUserInfoResponse.backendToken());

                User userInfoReturned = getUserInfoResponse
                        .data();

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
}
