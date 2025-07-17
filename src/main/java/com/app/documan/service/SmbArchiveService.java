package com.app.documan.service;

import com.app.documan.config.SMBProperty;
import com.app.documan.exception.DocumanException;
import com.app.documan.model.DocumentMetadata;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.Set;

import static com.app.documan.model.DocumentStatus.ERROR;
import static com.app.documan.model.DocumentStatus.ARCHIVED;


@Slf4j
@Service
public class SmbArchiveService {

    private final GridFSBucket gridFSBucket;
    private final SMBProperty smbProperty;
    private final DocumentService documentService;
    private final SMBClient client;

    public SmbArchiveService(GridFSBucket gridFSBucket, SMBProperty smbProperty, DocumentService documentService) {
        this.gridFSBucket = gridFSBucket;
        this.smbProperty = smbProperty;
        this.documentService = documentService;
        this.client = new SMBClient();
    }

    public void archiveToSmbStorage(String fileId) {

        DocumentMetadata metadata = documentService.getMetadataById(fileId).orElseThrow(() -> new DocumanException("File metadata not found: " + fileId));

        if(!metadata.isApproved()){
            log.warn("#RMQ05: File {}, {} is not approved. Only approved docs allowed to archive.", fileId, metadata.getOriginalFilename());
            return;
        }
        if(metadata.isArchived()){
            log.warn("#RMQ09: File {}, {} is already archived.", fileId, metadata.getOriginalFilename());
            return;
        }

        try (GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(fileId);
             Connection connection = client.connect(smbProperty.getHost(), smbProperty.getPort())) {

            AuthenticationContext ac = new AuthenticationContext(smbProperty.getUsername(), smbProperty.getPassword().toCharArray(), smbProperty.getDomain());
            Session session = connection.authenticate(ac);

            try (DiskShare share = (DiskShare) session.connectShare(smbProperty.getShareName())) {

                Set<AccessMask> accessMask = EnumSet.of(AccessMask.GENERIC_WRITE);
                Set<SMB2CreateOptions> createOptions = EnumSet.of(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE);
                Set<SMB2ShareAccess> shareAccess = EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ);
                try (File file = share.openFile(metadata.getOriginalFilename(),
                        accessMask,
                        EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                        shareAccess,
                        SMB2CreateDisposition.FILE_OVERWRITE_IF,
                        createOptions);
                     OutputStream smbOut = file.getOutputStream()) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = downloadStream.read(buffer)) != -1) {
                        smbOut.write(buffer, 0, bytesRead);
                    }
                    smbOut.flush();
                    log.info("#RMQ15: Archived file {}, {} to SMB server: {}", fileId, metadata.getOriginalFilename(), smbProperty.getShareName());
                }
            }
            documentService.updateMetadata(metadata, ARCHIVED);

        } catch (IOException e) {
            documentService.updateMetadata(metadata, ERROR);
            throw new DocumanException("Error during file archive to SMB server", e);
        }
    }
}
