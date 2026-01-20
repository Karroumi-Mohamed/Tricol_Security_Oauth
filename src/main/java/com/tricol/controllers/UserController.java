package com.tricol.controllers;

import com.tricol.dtos.request.PermissionOverrideRequest;
import com.tricol.dtos.request.UpdateRoleRequest;
import com.tricol.dtos.response.UserResponse;
import com.tricol.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_USERS')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<Void> updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        userService.updateUserRole(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/permissions")
    public ResponseEntity<Void> addPermissionOverride(@PathVariable Long id, @Valid @RequestBody PermissionOverrideRequest request) {
        userService.addPermissionOverride(id, request);
        return ResponseEntity.ok().build();
    }
}
