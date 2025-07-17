package com.app.documan.messaging;

import com.app.documan.service.SmbArchiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.app.documan.util.AppConstant.QUEUE_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQListener {

    private final SmbArchiveService smbTransferService;

    @RabbitListener(queues = QUEUE_NAME)
    public void handleDocumentMessage(String documentId) {
        log.info("#RMQ03: Received message from queue for document ID: {}", documentId);
        doArchive(List.of(documentId));

    }

    private void doArchive(List<String> documentIdList) {
        try {
            documentIdList.forEach(smbTransferService::archiveToSmbStorage);
        } catch (Exception e) {
            log.error("#RMQ09: Approving failed for document {}: {}", documentIdList, e.getMessage(), e);
        }
    }
}
