package com.davidparker.dms.repository;

import com.davidparker.dms.model.RegisteredApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegisteredApplicationRepository extends JpaRepository<RegisteredApplication, UUID> {
    
    Optional<RegisteredApplication> findByEntraAppId(String entraAppId);
    
    Optional<RegisteredApplication> findByApplicationName(String applicationName);
    
    boolean existsByEntraAppId(String entraAppId);
    
    boolean existsByApplicationName(String applicationName);
}
