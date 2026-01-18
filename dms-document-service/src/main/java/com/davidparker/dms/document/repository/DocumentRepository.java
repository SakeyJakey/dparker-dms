package com.davidparker.dms.document.repository;

import com.davidparker.dms.document.model.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    
    Page<Document> findByApplicationId(UUID applicationId, Pageable pageable);
    
    @Query("SELECT d FROM Document d WHERE d.applicationId = :applicationId AND d.classification = :classification")
    Page<Document> findByApplicationIdAndClassification(
        @Param("applicationId") UUID applicationId,
        @Param("classification") Document.Classification classification,
        Pageable pageable
    );
    
    List<Document> findByApplicationId(UUID applicationId);
    
    List<Document> findByIdAndApplicationId(UUID id, UUID applicationId);
}
