package com.tricol.security;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.tricol.entities.Permission;
import com.tricol.entities.RoleApp;
import com.tricol.entities.UserApp;
import com.tricol.repositories.PermissionRepository;
import com.tricol.repositories.RoleAppRepository;
import com.tricol.repositories.UserAppRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final RoleAppRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserAppRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Product Permissions
        Permission createProduct = createPermissonIfNotFound("CREATE_PRODUCT");
        Permission updateProduct = createPermissonIfNotFound("UPDATE_PRODUCT");
        Permission deleteProduct = createPermissonIfNotFound("DELETE_PRODUCT");
        Permission viewProduct = createPermissonIfNotFound("VIEW_PRODUCT");

        // Order Permissions
        Permission createOrder = createPermissonIfNotFound("CREATE_ORDER");
        Permission updateOrder = createPermissonIfNotFound("UPDATE_ORDER");
        Permission deleteOrder = createPermissonIfNotFound("DELETE_ORDER");
        Permission viewOrder = createPermissonIfNotFound("VIEW_ORDER");
        Permission receiveOrder = createPermissonIfNotFound("RECEIVE_ORDER");
        Permission cancelOrder = createPermissonIfNotFound("CANCEL_ORDER");
        Permission validateOrder = createPermissonIfNotFound("VALIDATE_ORDER");

        // Stock Permissions
        Permission viewStock = createPermissonIfNotFound("VIEW_STOCK");
        Permission manageStock = createPermissonIfNotFound("MANAGE_STOCK");
        Permission viewStockValuation = createPermissonIfNotFound("VIEW_STOCK_VALUATION");
        Permission viewStockMovements = createPermissonIfNotFound("VIEW_STOCK_MOVEMENTS");
        Permission configureStockAlerts = createPermissonIfNotFound("CONFIGURE_STOCK_ALERTS");

        // Delivery Note Permissions
        Permission createDeliveryNote = createPermissonIfNotFound("CREATE_DELIVERY_NOTE");
        Permission updateDeliveryNote = createPermissonIfNotFound("UPDATE_DELIVERY_NOTE");
        Permission deleteDeliveryNote = createPermissonIfNotFound("DELETE_DELIVERY_NOTE");
        Permission viewDeliveryNote = createPermissonIfNotFound("VIEW_DELIVERY_NOTE");
        Permission validateDeliveryNote = createPermissonIfNotFound("VALIDATE_DELIVERY_NOTE");
        Permission cancelDeliveryNote = createPermissonIfNotFound("CANCEL_DELIVERY_NOTE");

        // Supplier Permissions
        Permission createSupplier = createPermissonIfNotFound("CREATE_SUPPLIER");
        Permission updateSupplier = createPermissonIfNotFound("UPDATE_SUPPLIER");
        Permission deleteSupplier = createPermissonIfNotFound("DELETE_SUPPLIER");
        Permission viewSupplier = createPermissonIfNotFound("VIEW_SUPPLIER");

        // User Management Permissions
        Permission manageUsers = createPermissonIfNotFound("MANAGE_USERS");
        Permission viewAuditLogs = createPermissonIfNotFound("VIEW_AUDIT_LOGS");

        RoleApp userRole = createRoleIfNotFound("USER", Set.of());

        RoleApp adminRole = createRoleIfNotFound("ADMIN", Set.of(
                createProduct, updateProduct, deleteProduct, viewProduct, configureStockAlerts,
                createOrder, updateOrder, deleteOrder, viewOrder, receiveOrder, cancelOrder, validateOrder,
                viewStock, manageStock, viewStockValuation, viewStockMovements,
                createDeliveryNote, updateDeliveryNote, deleteDeliveryNote, viewDeliveryNote, validateDeliveryNote,
                cancelDeliveryNote,
                createSupplier, updateSupplier, deleteSupplier, viewSupplier,
                manageUsers, viewAuditLogs));

        createRoleIfNotFound("MAGAZINIER", Set.of(
                viewStock, manageStock, viewStockMovements,
                viewProduct,
                createDeliveryNote, viewDeliveryNote, validateDeliveryNote, cancelDeliveryNote,
                viewOrder));

        createRoleIfNotFound("RESP_ACHATS", Set.of(
                // Order Management per matrix
                createOrder, updateOrder, cancelOrder, viewOrder, receiveOrder,
                viewStock, viewStockMovements, viewStockValuation,
                createSupplier, updateSupplier, deleteSupplier, viewSupplier,
                // Implied
                viewProduct));

        createRoleIfNotFound("CHEF_ATELIER", Set.of(
                viewStock, viewStockMovements,
                createDeliveryNote, viewDeliveryNote,
                // Implied
                viewProduct));

        if (!userRepository.existsByUsername("admin")) {
            UserApp admin = UserApp.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(adminRole)
                    .active(true)
                    .build();

            userRepository.save(admin);
            System.out.println("Admin user created with username 'admin' and password 'admin123'");
        }
    }

    private Permission createPermissonIfNotFound(String name) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(Permission.builder().name(name).build()));
    }

    private RoleApp createRoleIfNotFound(String name, Set<Permission> permissions) {
        return roleRepository.findByName(name)
                .map(role -> {
                    role.setPermissions(permissions);
                    return roleRepository.save(role);
                })
                .orElseGet(() -> roleRepository.save(RoleApp.builder().name(name).permissions(permissions).build()));
    }
}
