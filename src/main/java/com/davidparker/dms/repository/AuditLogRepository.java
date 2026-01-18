package com.davidparker.dms.repository;

import com.davidparker.dms.model.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditEvent, UUID> {
    
    Page<AuditEvent> findByApplicationId(UUID applicationId, Pageable pageable);
    
    Page<AuditEvent> findByEventType(AuditEvent.EventType eventType, Pageable pageable);
    
    @Query("SELECT a FROM AuditEvent a WHERE a.timestamp BETWEEN :startTime AND :endTime")
    Page<AuditEvent> findByTimestampBetween(
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime,
        Pageable pageable
    );
    
    @Query("SELECT a FROM AuditEvent a WHERE a.correlationId = :correlationId")
    List<AuditEvent> findByCorrelationId(@Param("correlationId") UUID correlationId);
    
    Page<AuditEvent> findByPciRelevantTrue(Pageable pageable);
    
    Page<AuditEvent> findByGdprRelevantTrue(Pageable pageable);
}
