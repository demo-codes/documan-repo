package com.app.documan.service;

import com.app.documan.exception.DocumanException;
import com.app.documan.model.DocumentMetadata;
import com.app.documan.model.DocumentStatus;
import com.app.documan.repository.DocumentFileRepository;
import com.app.documan.repository.DocumentMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.app.documan.model.DocumentStatus.NEW;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentMetadataRepository metadataRepository;
    private final DocumentFileRepository fileRepository;

    public DocumentMetadata saveDocument(MultipartFile file, String title, String description) {
        try {
            String id = UUID.randomUUID().toString();

            fileRepository.saveFile(id, file.getBytes());

            DocumentMetadata metadata = DocumentMetadata.builder()
                    .id(id)
                    .title(title)
                    .description(description)
                    .originalFilename(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .status(NEW)
                    .uploadedAt(java.time.OffsetDateTime.now())
                    .build();
            log.info("#DS03: Saving document metadata: {}", metadata);
            return metadataRepository.save(metadata);
        } catch (Exception e) {
            throw new DocumanException("Failed to save document: " + title, e);
        }
    }

    public List<DocumentMetadata> getAllMetadata() {
        return metadataRepository.findAll();
    }

    public Optional<DocumentMetadata> getMetadataById(String id) {
        return metadataRepository.findById(id);
    }

    public boolean deleteDocument(String id) {
        log.info("#DS05: Deleting document with ID: {}", id);
        if(!metadataRepository.existsById(id)){
            log.info("#DS09: Document with ID: {} does not exist to delete", id);
            return false;
        }
        metadataRepository.deleteById(id);
        fileRepository.deleteFile(id);
        return true;
    }

    public ResponseEntity<byte[]> downloadDocument(String id) {
        byte[] data = fileRepository.getFile(id);

        if(data == null) {
            return ResponseEntity.notFound().build();
        }

        return metadataRepository.findById(id)
                .map(meta -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + meta.getOriginalFilename() + "\"")
                        .contentType(MediaType.parseMediaType(meta.getContentType()))
                        .body(data)
                )
                .orElse(ResponseEntity.notFound().build());
    }

    public void updateMetadata(DocumentMetadata metadata, DocumentStatus status) {
        metadata.setStatus(status);
        metadataRepository.save(metadata);
    }

    public List<String> findIdsOfNewDocuments() {
        return findIdsByStatus(NEW);
    }

    private List<String> findIdsByStatus(DocumentStatus documentStatus) {
        List<DocumentMetadata> docs = metadataRepository.findAllByStatus(documentStatus);
        if(CollectionUtils.isEmpty(docs)){
            log.info("#DS21: No documents found for status: {}", documentStatus);
            return List.of();
        }
        return docs.stream().map(DocumentMetadata::getId).toList();
    }
}
