package com.dropbox.server.exception;

public class DropboxException extends RuntimeException {

    private final DropboxExceptionType type;

    public DropboxException(DropboxExceptionType type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
    }

    public DropboxExceptionType getType(){
        return type;
    }
}

