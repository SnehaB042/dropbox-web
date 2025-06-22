package com.dropbox.server.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
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
@RequestMapping("/api")
public class DropboxController {

    @Autowired
    private FileService fileService;
    
    @PostMapping("/upload")
    public ResponseEntity<FileMetaDataResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        System.out.println("recieved request file : " + file.getOriginalFilename());
        FileMetaDataResponse response = fileService.uploadFile(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/list")
    public ResponseEntity<FileListResponse> getAllFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noStore());
        
        FileListResponse response = fileService.getAllFiles(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        FileMetaDataResponse metadata = fileService.getFileMetadata(id);

        Resource resource = fileService.downloadFile(id);
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            contentType = metadata.getMimeType();
        }
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
        
        FileMetaDataResponse metadata = fileService.getFileMetadata(id);
        
        if (!FileUtils.isViewableFileType(metadata.getFileExtension())) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .header("X-Error", "File type not supported for viewing")
                    .build();
        }

        Resource resource = fileService.downloadFile(id);
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
