package com.dropbox.server.utils;

import java.util.UUID;

import org.apache.commons.io.FilenameUtils;

public class FileUtils {
    public static String generateStoredFilename(String displayFilename) {
        String extension = FilenameUtils.getExtension(displayFilename);
        String uuid = UUID.randomUUID().toString();
        
        if (extension.isEmpty()) {
            return uuid;
        } else {
            return uuid + "." + extension;
        }
    }
    
    public static String preprocessFilename(String filename) {
        if (filename == null) {
            return "unnamed_file";
        }
        
        String preprocessed = filename.replaceAll("[^a-zA-Z0-9\\.\\-_\\(\\)\\s]", "_");
        preprocessed = preprocessed.replaceAll("_{2,}", "_");
        preprocessed = preprocessed.trim();

        if (preprocessed.isEmpty()) {
            return "unnamed_file";
        }
        
        if (preprocessed.length() > 255) {
            String extension = FilenameUtils.getExtension(preprocessed);
            String name = FilenameUtils.removeExtension(preprocessed);
            name = name.substring(0, 255 - extension.length() - 1);
            preprocessed = name + "." + extension;
        }
        
        return preprocessed;
    }
    
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    public static boolean isImageFile(String fileExtension) {
        return switch (fileExtension.toLowerCase()) {
            case "jpg", "jpeg", "png", "gif", "bmp", "webp" -> true;
            default -> false;
        };
    }
    
    public static boolean isTextFile(String fileExtension) {
        return switch (fileExtension.toLowerCase()) {
            case "txt", "json", "xml", "csv", "log", "md" -> true;
            default -> false;
        };
    }

    public static boolean isViewableFileType(String fileExtension) {
        return switch (fileExtension.toLowerCase()) {
            case "txt", "json", "jpg", "jpeg", "png", "pdf" -> true;
            default -> false;
        };
    }
    
    public static String determineViewContentType(String fileExtension, String mimeType) {
        return switch (fileExtension.toLowerCase()) {
            case "txt" -> "text/plain";
            case "json" -> "application/json";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "pdf" -> "application/pdf";
            default -> mimeType != null ? mimeType : "application/octet-stream";
        };
    }
    
}
