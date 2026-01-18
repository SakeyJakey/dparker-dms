package com.davidparker.dms.service;

import com.davidparker.dms.model.Permission;
import com.davidparker.dms.model.Role;
import com.davidparker.dms.model.User;
import com.davidparker.dms.repository.PermissionRepository;
import com.davidparker.dms.repository.RoleRepository;
import com.davidparker.dms.repository.UserRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final Cache<String, Set<String>> permissionCache;

    public PermissionService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.permissionCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(1000)
            .build();
    }

    public boolean hasPermission(UUID userId, String permissionName) {
        String cacheKey = userId + ":" + permissionName;
        return permissionCache.get(cacheKey, key -> {
            User user = userRepository.findById(userId)
                .orElse(null);
            
            if (user == null || !user.getEnabled()) {
                return Set.of();
            }

            Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());

            return permissions;
        }).contains(permissionName);
    }

    public boolean hasRole(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
            .orElse(null);
        
        if (user == null || !user.getEnabled()) {
            return false;
        }

        return user.getRoles().stream()
            .anyMatch(role -> role.getName().equals(roleName));
    }

    public void clearPermissionCache(UUID userId) {
        permissionCache.asMap().keySet().removeIf(key -> key.startsWith(userId.toString()));
    }
}
