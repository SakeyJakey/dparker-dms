package com.davidparker.dms.admin.repository;

import com.davidparker.dms.admin.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    Optional<ApiKey> findByKeyHashAndActiveTrue(String keyHash);
    Optional<ApiKey> findByKeyPrefixAndActiveTrue(String keyPrefix);
}
