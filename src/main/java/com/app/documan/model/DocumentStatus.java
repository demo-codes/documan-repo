package com.app.documan.model;

public enum DocumentStatus {
    NEW,
    APPROVED,
    ARCHIVED,
    ERROR;

    DocumentStatus() {
    }

    public boolean isApproved() {
        return this.equals(APPROVED) || this.equals(ARCHIVED);
    }
}
