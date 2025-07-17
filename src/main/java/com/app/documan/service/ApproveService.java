package com.app.documan.service;

import com.app.documan.model.DocumentMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Optional;

import static com.app.documan.model.DocumentStatus.APPROVED;
import static com.app.documan.model.DocumentStatus.NEW;
import static com.app.documan.util.AppConstant.EXCHANGE_NAME;
import static com.app.documan.util.AppConstant.ROUTING_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApproveService {

    private final RabbitTemplate rabbitTemplate;
    private final DocumentService documentService;

    public HttpStatus sendToApprove(String documentId) {
        Optional<DocumentMetadata> result = documentService.getMetadataById(documentId);
        if(result.isEmpty()) {
            log.error("#PS05: Document metadata to approve not found: {}", documentId);
            return HttpStatus.NOT_FOUND;
        }
        var foundMetadata = result.get();
        log.info("#PS03: Document metadata found: {}", foundMetadata);
        if(!NEW.equals(foundMetadata.getStatus())){
            log.warn("#PS07: Document metadata status is invalid to approve: {}", foundMetadata);
            return HttpStatus.BAD_REQUEST;
        }
        documentService.updateMetadata(foundMetadata, APPROVED);
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, documentId); // send a request to archive the document
        return HttpStatus.OK;
    }

    public int sendAllToApprove() {
        var documentIds = documentService.findIdsOfNewDocuments();
        if(CollectionUtils.isEmpty(documentIds)) {
            log.info("#PS21: No new documents found to approve.");
            return 0;
        }
        documentIds.forEach(this::sendToApprove);
        return documentIds.size();
    }

}