package com.tricol.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tricol.dtos.request.PermissionOverrideRequest;
import com.tricol.dtos.request.UpdateRoleRequest;
import com.tricol.dtos.response.UserResponse;
import com.tricol.entities.Permission;
import com.tricol.entities.RoleApp;
import com.tricol.entities.UserApp;
import com.tricol.entities.UserPermission;
import com.tricol.exceptions.ResourceNotFoundException;
import com.tricol.repositories.PermissionRepository;
import com.tricol.repositories.RoleAppRepository;
import com.tricol.repositories.UserAppRepository;
import com.tricol.repositories.UserPermissionRepository;
import com.tricol.services.AuditLogService;
import com.tricol.services.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserAppRepository userRepository;
    private final RoleAppRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final AuditLogService auditLogService;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Long id) {
        UserApp user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
        auditLogService.log(null, "DELETE_USER", "User", id, "User deleted");
    }

    @Override
    @Transactional
    public void updateUserRole(Long userId, UpdateRoleRequest request) {
        UserApp user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RoleApp role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRole(role);
        userRepository.save(user);
        auditLogService.log(user.getUsername(), "UPDATE_ROLE", "User", userId,
                "Role changed to " + request.getRoleName());
    }

    @Override
    @Transactional
    public void addPermissionOverride(Long userId, PermissionOverrideRequest request) {
        UserApp user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Permission permission = permissionRepository.findByName(request.getPermissionName())
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        var existingOverride = userPermissionRepository
                .findByUserIdAndPermissionId(userId, permission.getId());

        if (existingOverride.isPresent()) {
            existingOverride.get().setAction(request.getAction());
            userPermissionRepository.save(existingOverride.get());
        } else {
            UserPermission newOverride = UserPermission.builder()
                    .user(user)
                    .permission(permission)
                    .action(request.getAction())
                    .build();
            userPermissionRepository.save(newOverride);
        }
        auditLogService.log(user.getUsername(), "PERMISSION_OVERRIDE", "User", userId,
                request.getAction() + " " + request.getPermissionName());
    }

    private UserResponse mapToResponse(UserApp user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .active(user.isActive())
                .build();
    }
}
