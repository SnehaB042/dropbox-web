package com.dropbox.server.service;

import com.dropbox.server.config.FileStorageProperties;
import com.dropbox.server.dto.FileListResponse;
import com.dropbox.server.dto.FileMetaDataResponse;
import com.dropbox.server.model.FileMetadata;
import com.dropbox.server.exception.DropboxException;
import com.dropbox.server.exception.DropboxExceptionType;
import com.dropbox.server.repository.FileRepository;
import com.dropbox.server.utils.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FileService {
    
    @Autowired
    private FileRepository fileRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private FileStorageProperties fileStorageProperties;
    
    private final Tika tika = new Tika();
    
    public FileMetaDataResponse uploadFile(MultipartFile file) {
        validateFile(file);
        
        String originalFilename = file.getOriginalFilename();
        String displayFilename = resolveDisplayFilename(originalFilename);
        
        try {
            String checksum = calculateChecksum(file);
            
            String storedFilePath = fileStorageService.storeFile(file, displayFilename);
            
            String mimeType = tika.detect(file.getInputStream(), originalFilename);
            String fileExtension = FilenameUtils.getExtension(originalFilename).toLowerCase();
            
            int duplicateSequence = getDuplicateSequence(originalFilename, displayFilename);
            
            FileMetadata fileEntity = new FileMetadata(
                    originalFilename,
                    displayFilename,
                    FileUtils.generateStoredFilename(displayFilename),
                    storedFilePath,
                    file.getSize(),
                    mimeType,
                    fileExtension,
                    checksum,
                    duplicateSequence
            );
            
            fileEntity = fileRepository.save(fileEntity);
            
            return new FileMetaDataResponse(
                    fileEntity.getId(),
                    fileEntity.getDisplayFilename(),
                    fileEntity.getOriginalFilename(),
                    fileEntity.getFileSize(),
                    fileEntity.getMimeType(),
                    fileEntity.getFileExtension(),
                    fileEntity.getUploadTimestamp(),
                    fileEntity.getDuplicateSequence(),
                    fileEntity.getChecksum()
            );
            
        } catch (IOException ex) {
            throw new DropboxException(DropboxExceptionType.FILE_STORAGE_ERROR, "Failed to upload file: " + originalFilename, ex);
        }
    }
    
    @Transactional(readOnly = true)
    public FileListResponse getAllFiles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FileMetadata> filePage = fileRepository.findAllOrderByUploadTimeDesc(pageable);

        System.out.println("page list elements : " + filePage.getNumberOfElements());
        
        List<FileMetaDataResponse> files = filePage.getContent().stream()
                .map(this::convertToMetadataResponse)
                .collect(Collectors.toList());
        
        return new FileListResponse(
                files,
                filePage.getNumberOfElements(),
                page,
                filePage.getTotalPages(),
                filePage.getTotalElements()
        );
    }
    
    @Transactional
    public Resource downloadFile(Long fileId) throws DropboxException{
        try {
        FileMetadata fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new DropboxException(DropboxExceptionType.FILE_NOT_FOUND, "File not found with id: " + fileId, null));        
            return fileStorageService.loadFileAsResource(fileEntity.getFilePath());
        } catch(DropboxException e){
            throw e;
        }
        catch(Exception e){
            throw new DropboxException(DropboxExceptionType.UNKNOWN_ERROR, "Internal server error occured", e);
        }
    }
    
    @Transactional(readOnly = true)
    public FileMetaDataResponse getFileMetadata(Long fileId) {
        FileMetadata fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new DropboxException(DropboxExceptionType.FILE_NOT_FOUND, "File not found with id: " + fileId, null));
        
        return convertToMetadataResponse(fileEntity);
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new DropboxException(DropboxExceptionType.INVALID_FILE, "Cannot upload empty file", null);
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new DropboxException(DropboxExceptionType.INVALID_FILE, "Filename cannot be empty", null);
        }
        
        // Check file size
        if (file.getSize() > fileStorageProperties.getMaxSize()) {
            throw new DropboxException(DropboxExceptionType.INVALID_FILE, 
                    String.format("File size exceeds maximum allowed size of %d bytes", 
                            fileStorageProperties.getMaxSize()), null);
        }
        
        // Check file extension
        String fileExtension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        if (!fileStorageProperties.getAllowedTypes().contains(fileExtension)) {
            throw new DropboxException( DropboxExceptionType.INVALID_FILE, 
                    String.format("File type '%s' is not allowed. Allowed types: %s", 
                            fileExtension, fileStorageProperties.getAllowedTypes()), null);
        }
    }
    
    private synchronized String resolveDisplayFilename(String originalFilename) {
        List<FileMetadata> existingFiles = fileRepository
                .findByOriginalFilenameOrderByDuplicateSequenceDesc(originalFilename);
        
        if (existingFiles.isEmpty()) {
            return originalFilename;
        }
        
        int highestSequence = existingFiles.get(0).getDuplicateSequence();
        int nextSequence = highestSequence + 1;
        
        if (nextSequence > fileStorageProperties.getMaxDuplicatesPerName()) {
            throw new DropboxException(DropboxExceptionType.INVALID_FILE,
                    String.format("Maximum number of duplicates (%d) exceeded for filename: %s",
                            fileStorageProperties.getMaxDuplicatesPerName(), originalFilename), null);
        }
        
        return generateDuplicateName(originalFilename, nextSequence);
    }
    
    private String generateDuplicateName(String originalFilename, int sequence) {
        String nameWithoutExt = FilenameUtils.removeExtension(originalFilename);
        String extension = FilenameUtils.getExtension(originalFilename);
        
        if (extension.isEmpty()) {
            return String.format("%s(%d)", nameWithoutExt, sequence);
        } else {
            return String.format("%s(%d).%s", nameWithoutExt, sequence, extension);
        }
    }
    
    private int getDuplicateSequence(String originalFilename, String displayFilename) {
        if (originalFilename.equals(displayFilename)) {
            return 0;
        }
        
        String nameWithoutExt = FilenameUtils.removeExtension(displayFilename);
        if (nameWithoutExt.contains("(") && nameWithoutExt.endsWith(")")) {
            String sequencePart = nameWithoutExt.substring(
                    nameWithoutExt.lastIndexOf("(") + 1, nameWithoutExt.lastIndexOf(")"));
            try {
                return Integer.parseInt(sequencePart);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    
    private String calculateChecksum(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append(hex);
            }
        }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException ex) {
            throw new DropboxException(DropboxExceptionType.FILE_STORAGE_ERROR, "Failed to calculate file checksum", ex);
        }
    }
    
    private FileMetaDataResponse convertToMetadataResponse(FileMetadata fileEntity) {
        System.out.println("file meta data : " + fileEntity);
        return new FileMetaDataResponse(
                fileEntity.getId(),
                fileEntity.getDisplayFilename(),
                fileEntity.getOriginalFilename(),
                fileEntity.getFileSize(),
                fileEntity.getMimeType(),
                fileEntity.getFileExtension(),
                fileEntity.getUploadTimestamp(),
                fileEntity.getDuplicateSequence(),
                fileEntity.getChecksum()
        );
    }
}