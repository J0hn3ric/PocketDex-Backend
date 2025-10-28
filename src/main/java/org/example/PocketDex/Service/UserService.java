package org.example.PocketDex.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.PocketDex.DTO.response.ResponseBodyDTO;
import org.example.PocketDex.DTO.response.UpdateUserProfileResponseDTO;
import org.example.PocketDex.Model.User;
import org.example.PocketDex.Repository.user.UserRepository;
import org.example.PocketDex.Service.utils.SessionUtils;
import org.example.PocketDex.Utils.SupabaseConstants;
import org.example.PocketDex.Utils.UserConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final SessionService sessionService;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();



    @Autowired
    public UserService(
            JWTService jwtService,
            SessionService sessionService,
            TokenService tokenService,
            @Qualifier("supabaseUserRepository") UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.tokenService = tokenService;
        this.sessionService = sessionService;
        this.userRepository = userRepository;

    }

    public Mono<ResponseBodyDTO<Void>> createNewUser(
            String friendId,
            String username,
            String userImg
    ) {
        return userRepository.signup().flatMap(response -> {
            String accessToken = response.get(UserConstants.ACCESS_TOKEN_KEY).asText();
            long accessTokenExpiration = response.get("expires_at").asLong();
            String refreshToken = response.get(UserConstants.REFRESH_TOKEN_KEY). asText();
            String userId = response.get("user").get("id").asText();

            String backendToken = sessionService.createSession(
                    accessToken, accessTokenExpiration, refreshToken
            );

            User user = new User(
                    userId,
                    friendId,
                    username,
                    userImg
            );

            return userRepository.createUser(user, accessToken)
                    .then(Mono.just(new ResponseBodyDTO<>(null, backendToken)));
        });
    }


    public Mono<ResponseBodyDTO<User>> getOwnUserInfo(String backendToken) {
        return fetchUser(backendToken, null);
    }

    public Mono<ResponseBodyDTO<User>> getUserInfoById(String backendToken, String userId) {
        return fetchUser(backendToken, userId);
    }

    public Mono<ResponseBodyDTO<List<User>>> getUserInfoByUsername(String backendToken, String username) {
        return tokenService.withValidSession(backendToken, sessionContext -> {
            String accessToken = SessionUtils.getAccessToken(sessionContext);
            String newBackendToken = SessionUtils.getBackendToken(sessionContext);

            return userRepository.getUsersByUsername(username, accessToken)
                    .map(users -> new ResponseBodyDTO<>(users, newBackendToken));
        });
    }

    public Mono<ResponseBodyDTO<UpdateUserProfileResponseDTO>> updateUserInfo(
            String backendToken, String newUsername, String newUserImg
    ) {
        return tokenService.withValidSession(backendToken, sessionContext -> {
            String accessToken = SessionUtils.getAccessToken(sessionContext);
            String newBackendToken = SessionUtils.getBackendToken(sessionContext);
            UUID userId = SessionUtils.getUserId(accessToken, jwtService);

            ObjectNode payload = objectMapper.createObjectNode();

            if (newUsername != null && !newUsername.isEmpty()) {
                payload.put("username", newUsername);
            }

            if (newUserImg != null && !newUserImg.isEmpty()) {
                payload.put("user_img", newUserImg);
            }

            System.out.println(payload.toString());

            if (!payload.isEmpty()) {
                return userRepository.updateUser(userId.toString(), payload, accessToken)
                        .map(user -> new ResponseBodyDTO<>(user, newBackendToken));
            } else {
                throw new IllegalArgumentException("Illegal Argument: got empty request body");
            }
        });
    }

    public Mono<ResponseBodyDTO<String>> deleteUserUsingBackendToken(String backendToken) {
        return tokenService.withValidSession(backendToken, sessionContext -> {
            String accessToken = SessionUtils.getAccessToken(sessionContext);
            UUID userId = SessionUtils.getUserId(accessToken, jwtService);

            sessionService.deleteSession(backendToken);

            return userRepository.deleteUser(userId.toString())
                    .then(Mono.just(new ResponseBodyDTO<>("user deleted successfully!", null)));
        });
    }

    public Mono<Void> deleteUserUsingUserId(String userId) {
        return userRepository.deleteUser(userId);
    }

    private Mono<ResponseBodyDTO<User>> fetchUser(String backendToken, String id) {
        return tokenService.withValidSession(backendToken, sessionContext -> {
            String accessToken = SessionUtils.getAccessToken(sessionContext);
            String newBackendToken = SessionUtils.getBackendToken(sessionContext);

            String userId = id == null
                ? jwtService.getUserIdFromToken(accessToken)
                : id;

            return userRepository.getUserById(userId, accessToken)
                    .map(user -> new ResponseBodyDTO<>(user, newBackendToken));
        });
    }

}
