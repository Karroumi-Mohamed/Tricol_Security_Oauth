package com.tricol.security;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tricol.entities.Permission;
import com.tricol.entities.UserApp;
import com.tricol.entities.UserPermission;
import com.tricol.entities.enums.PermissionAction;
import com.tricol.repositories.UserAppRepository;
import com.tricol.repositories.UserPermissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserAppRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserApp user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Set<String> finalPermissions = new HashSet<>();

        if (user.getRole() != null) {
            for (Permission permission : user.getRole().getPermissions()) {
                finalPermissions.add(permission.getName());
            }
        }

        List<UserPermission> overrides = userPermissionRepository.findByUserId(user.getId());
        for (UserPermission override : overrides) {
            if (override.getAction() == PermissionAction.GRANT) {
                finalPermissions.add(override.getPermission().getName());
            } else if (override.getAction() == PermissionAction.REVOKE) {
                finalPermissions.remove(override.getPermission().getName());
            }
        }

        List<? extends GrantedAuthority> authorities = finalPermissions.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new User(
                user.getUsername(),
                user.getPassword(),
                user.isActive(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities);
    }
}
