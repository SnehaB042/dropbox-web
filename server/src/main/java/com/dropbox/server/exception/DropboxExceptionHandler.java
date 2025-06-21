package com.dropbox.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class DropboxExceptionHandler extends ResponseEntityExceptionHandler {
    
    @ExceptionHandler(DropboxException.class)
    public ResponseEntity<Map<String, Object>> handleDropboxException(DropboxException ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                ex.getType().toString(), 
                ex.getMessage(), 
                mapErrorTypeToStatus(ex.getType())
        );
        return ResponseEntity.status(mapErrorTypeToStatus(ex.getType())).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                "INTERNAL_SERVER_ERROR", 
                "An unexpected error occurred: " + ex.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR
        );
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private HttpStatus mapErrorTypeToStatus(DropboxExceptionType type) {
        return switch (type) {
            case INVALID_FILE -> HttpStatus.BAD_REQUEST;
            case FILE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case FILE_STORAGE_ERROR -> HttpStatus.SERVICE_UNAVAILABLE;
            case UNKNOWN_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
    
    private Map<String, Object> createErrorResponse(String errorCode, String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("message", message);
        return errorResponse;
    }
}
