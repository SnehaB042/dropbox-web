package com.dropbox.server.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dropbox.server.dto.FileListResponse;
import com.dropbox.server.dto.FileMetaDataResponse;
import com.dropbox.server.service.FileService;
import com.dropbox.server.utils.FileUtils;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private FileService fileService;

    // @PostMapping("/upload")
    // public ResponseEntity<String> uploadFile(@RequestParam MultipartFile file) {
    //     if (file != null && !file.isEmpty()) {
    //         try {
    //             byte[] bytes = file.getBytes();
    //             logger.info("received file of size : {} bytes", bytes); // sum up, convert and print in KB
    //             // uploadService.store(file);
    //             return new ResponseEntity<>("File uploaded successfully: " + file.getOriginalFilename(), HttpStatus.OK);
    //         } catch (Exception e) {
    //             return new ResponseEntity<>("Failed to upload file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    //         }
    //     } else {
    //         return new ResponseEntity<>("No file selected for upload", HttpStatus.BAD_REQUEST);
    //     }
    // }
    
    @PostMapping("/upload")
    public ResponseEntity<FileMetaDataResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        System.out.println("request : " + file.getOriginalFilename());
        logger.info("request : ", file);
        FileMetaDataResponse response = fileService.uploadFile(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<FileListResponse> getAllFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        FileListResponse response = fileService.getAllFiles(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        // Get file metadata for headers
        FileMetaDataResponse metadata = fileService.getFileMetadata(id);
        
        // Load file as Resource
        Resource resource = fileService.downloadFile(id);
        
        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Fallback to metadata mime type
            contentType = metadata.getMimeType();
        }
        
        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + metadata.getDisplayFilename() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(metadata.getFileSize()))
                .header("X-File-Id", id.toString())
                .header("X-Upload-Time", metadata.getUploadTimestamp().toString())
                .body(resource);
    }

    @GetMapping("/{id}/metadata")
    public ResponseEntity<FileMetaDataResponse> getFileMetadata(@PathVariable Long id) {
        FileMetaDataResponse response = fileService.getFileMetadata(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewFile(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        // Get file metadata
        FileMetaDataResponse metadata = fileService.getFileMetadata(id);
        
        // Only allow viewing of safe file types
        if (!FileUtils.isViewableFileType(metadata.getFileExtension())) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .header("X-Error", "File type not supported for viewing")
                    .build();
        }
        
        // Load file as Resource
        Resource resource = fileService.downloadFile(id);
        
        // Determine content type for inline viewing
        String contentType = FileUtils.determineViewContentType(metadata.getFileExtension(), metadata.getMimeType());
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + metadata.getDisplayFilename() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(resource);
    }


    

}
