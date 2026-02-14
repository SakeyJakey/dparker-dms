package com.davidparker.dms.admin.repository;

import com.davidparker.dms.admin.model.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookRepository extends JpaRepository<Webhook, UUID> {
    List<Webhook> findByApplicationIdAndActiveTrue(UUID applicationId);
    List<Webhook> findByActiveTrue();
}
