package com.app.documan.controller;

import com.app.documan.model.DocumentMetadata;
import com.app.documan.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.app.documan.util.ObjectUtils.getNonBlankOrDefault;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    @Operation(summary = "Upload a document",
            description = "Upload a document with title and description.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DocumentMetadata> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description
    ) {
        DocumentMetadata saved = documentService.saveDocument(file, getNonBlankOrDefault(() -> title, file.getOriginalFilename()), getNonBlankOrDefault(() -> description, file.getOriginalFilename()));
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    @Operation(summary = "Retrieve all document metadata",
            description = "Retrieve all documents in the system. Returns an empty list if no documents are found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved document metadata list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DocumentMetadata.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<DocumentMetadata>> getAllDocuments() {
        return ResponseEntity.ok(documentService.getAllMetadata());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve document metadata by ID",
            description = "Retrieves the document identified by its unique ID. Returns 404 if the document is not found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved document metadata",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DocumentMetadata.class))),
            @ApiResponse(responseCode = "404", description = "Document with specified ID not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<DocumentMetadata> getDocument(@PathVariable String id) {
        return documentService.getMetadataById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document by ID",
            description = "Deletes a document using its unique ID. Returns 204 No Content on successful deletion.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Document successfully deleted",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Document with specified ID not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })    public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
        return documentService.deleteDocument(id) ? ResponseEntity.noContent().build() :  ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download a document by ID",
            description = "Downloads the file by its unique ID. Returns the document data as a byte array.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document successfully retrieved for download",
                    content = @Content(mediaType = "application/octet-stream",
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Document with specified ID not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })    public ResponseEntity<byte[]> downloadDocument(@PathVariable String id) {
        return documentService.downloadDocument(id);
    }
}
