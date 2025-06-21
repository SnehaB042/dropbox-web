package com.dropbox.server.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dropbox.server.config.FileStorageProperties;
import com.dropbox.server.exception.DropboxException;
import com.dropbox.server.exception.DropboxExceptionType;

@Service
public class FileStorageService {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Path fileStorageLocation;

    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getLocation())
                .toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            logger.error("Could not create upload directory", ex);
            throw new DropboxException(DropboxExceptionType.FILE_STORAGE_ERROR, "Could not create upload directory", ex);            
        }
    }
    
    public String storeFile(MultipartFile file, String displayFilename) {
        
        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path datePath = this.fileStorageLocation.resolve(dateDir);
            Files.createDirectories(datePath);
            
            String storedFilename = UUID.randomUUID().toString() + "_" + 
                    preprocessFileName(displayFilename);
            
            Path targetLocation = datePath.resolve(storedFilename);
        
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            return this.fileStorageLocation.relativize(targetLocation).toString();
            
        } catch (IOException ex) {
            logger.error("Could not store file " + displayFilename, ex);
            throw new DropboxException(DropboxExceptionType.FILE_STORAGE_ERROR, "Could not store file " + displayFilename, ex);
        }
    }
    
    public Resource loadFileAsResource(String relativePath) throws Exception {
        try {
            Path filePath = this.fileStorageLocation.resolve(relativePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                logger.error("File not found: " + relativePath);
                throw new DropboxException(DropboxExceptionType.FILE_NOT_FOUND, "File not found: " + relativePath, null);
            }
        } catch (MalformedURLException ex) {
            logger.error("File not found: " + relativePath, ex);
            throw new DropboxException(DropboxExceptionType.FILE_NOT_FOUND, "File not found: " + relativePath, ex);
        }
    }
    
    public boolean deleteFile(String relativePath) {
        try {
            Path filePath = this.fileStorageLocation.resolve(relativePath).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            return false;
        }
    }
    
    public long getFileSize(String relativePath) {
        try {
            Path filePath = this.fileStorageLocation.resolve(relativePath).normalize();
            return Files.size(filePath);
        } catch (IOException ex) {
            return 0;
        }
    }
    
    private String preprocessFileName(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9\\.\\-_\\(\\)]", "_");
    }
}
