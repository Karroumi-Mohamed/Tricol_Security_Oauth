package com.tricol.services;

import com.tricol.dtos.request.PermissionOverrideRequest;
import com.tricol.dtos.request.UpdateRoleRequest;
import com.tricol.dtos.response.UserResponse;
import com.tricol.entities.UserApp;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    void deleteUser(Long id);

    void updateUserRole(Long userId, UpdateRoleRequest request);

    void addPermissionOverride(Long userId, PermissionOverrideRequest request);

    UserApp findOrCreateKeycloakUser(String keycloakId, String username);
}
