package com.davidparker.dms.admin.controller;

import com.davidparker.dms.admin.dto.UserCreateRequest;
import com.davidparker.dms.admin.dto.UserUpdateRequest;
import com.davidparker.dms.admin.model.User;
import com.davidparker.dms.admin.service.UserManagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('DMS.Admin')")
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public ResponseEntity<Page<User>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(userManagementService.listUsers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userManagementService.getUser(id));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(userManagementService.createUser(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userManagementService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<User> assignRole(@PathVariable UUID id, @RequestParam UUID roleId) {
        return ResponseEntity.ok(userManagementService.assignRole(id, roleId));
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    public ResponseEntity<User> removeRole(@PathVariable UUID id, @PathVariable UUID roleId) {
        return ResponseEntity.ok(userManagementService.removeRole(id, roleId));
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<User> enableUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userManagementService.enableUser(id));
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<User> disableUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userManagementService.disableUser(id));
    }
}
