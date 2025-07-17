package com.app.documan.repository;

import com.app.documan.model.DocumentMetadata;
import com.app.documan.model.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, String> {
    List<DocumentMetadata> findAllByStatus(DocumentStatus status);
}
