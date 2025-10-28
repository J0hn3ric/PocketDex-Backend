package org.example.PocketDex.Repository.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.PocketDex.DTO.response.UpdateUserCardsResponseDTO;
import org.example.PocketDex.DTO.response.UpdateUserProfileResponseDTO;
import org.example.PocketDex.Model.User;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface UserRepository {
    Mono<JsonNode> signup();
    Mono<Void> createUser(User user, String accessToken);
    Mono<User> getUserById(String userId, String accessToken);
    Mono<List<User>> getUsersByUsername(String username, String accessToken);
    Mono<UpdateUserProfileResponseDTO> updateUser(String userId, ObjectNode updatedInfo, String accessToken);
    Mono<Void> deleteUser(String userId);
}
