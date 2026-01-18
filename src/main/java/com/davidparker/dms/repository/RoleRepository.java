package com.davidparker.dms.repository;

import com.davidparker.dms.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);
    
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    Optional<Role> findByPermissionName(@Param("permissionName") String permissionName);
}
