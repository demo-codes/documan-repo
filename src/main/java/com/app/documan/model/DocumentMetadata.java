package com.app.documan.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "document_metadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMetadata {

    @Id
    private String id;

    private String title;
    private String description;

    private String originalFilename;
    private String contentType;
    private Long size;

    private OffsetDateTime uploadedAt;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    public boolean isApproved() {
        return status != null && status.isApproved();
    }

    public boolean isArchived() {
        return DocumentStatus.ARCHIVED.equals(status);
    }
}