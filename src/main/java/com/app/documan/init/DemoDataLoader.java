package com.app.documan.init;

import com.app.documan.model.DocumentMetadata;
import com.app.documan.model.DocumentStatus;
import com.app.documan.repository.DocumentMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
//@Profile({"local", "test"})
@Profile({"test"})
public class DemoDataLoader implements CommandLineRunner {

    private final DocumentMetadataRepository metadataRepository;

    @Override
    public void run(String... args) {
        log.info("#DDL05: Start demo data loading...");
        if (metadataRepository.count() != 0) {
            log.info("#DDL09f: DB already contains the data, no demo data will be inserted.");
            return;
        }
        DocumentMetadata doc = DocumentMetadata.builder()
                .id(UUID.randomUUID().toString())
                .title("Demo dummy document")
                .uploadedAt(OffsetDateTime.now())
                .status(DocumentStatus.NEW)
                .build();
        metadataRepository.save(doc);
        log.info("#DDL09c: Inserted demo data with ID: {}", doc.getId());
    }
}
