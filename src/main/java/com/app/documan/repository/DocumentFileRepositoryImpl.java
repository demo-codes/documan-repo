package com.app.documan.repository;

import com.app.documan.exception.DocumanException;
import com.mongodb.MongoGridFSException;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayOutputStream;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DocumentFileRepositoryImpl implements DocumentFileRepository {

    private final GridFSBucket gridFSBucket;

    @Override
    public void saveFile(String id, byte[] data) {
        GridFSUploadOptions options = new GridFSUploadOptions();
        try (GridFSUploadStream stream = gridFSBucket.openUploadStream(id, options)) {
            stream.write(data);
        } catch (Exception e) {
            throw new DocumanException("Could not upload file to MongoDB GridFS: " + id, e);
        }
    }

    @Override
    public byte[] getFile(String id) {
        try (GridFSDownloadStream stream = gridFSBucket.openDownloadStream(id);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = stream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            return output.toByteArray();
        } catch (MongoGridFSException e) {
            log.warn("#DFR05: File to download not found in MongoDB GridFS: {}", id);
            return null;
        } catch (Exception e) {
            throw new DocumanException("Could not read file from MongoDB GridFS: " + id, e);
        }
    }

    @Override
    public void deleteFile(String id) {
        gridFSBucket.find().forEach(file -> {
            if (file.getFilename().equals(id)) {
                gridFSBucket.delete(file.getObjectId());
            }
        });
    }
}
