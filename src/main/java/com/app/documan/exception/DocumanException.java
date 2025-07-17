package com.app.documan.exception;

public class DocumanException extends RuntimeException {

    public DocumanException(String message) {
        super(message);
    }

    public DocumanException(String message, Throwable cause) {
        super(message, cause);
    }
}