package com.app.documan.repository;

public interface DocumentFileRepository {

    void saveFile(String id, byte[] data);

    byte[] getFile(String id);

    void deleteFile(String id);
}
