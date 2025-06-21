package com.dropbox.server.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileListResponse {
    private List<FileMetaDataResponse> files;
    private long totalFiles;
    private long totalSize;
    private int currentPage;
    private int totalPages;
    private long totalElements;

    public FileListResponse() {}
    
    public FileListResponse(List<FileMetaDataResponse> files, long totalFiles, long totalSize,
                           int currentPage, int totalPages, long totalElements) {
        this.files = files;
        this.totalFiles = totalFiles;
        this.totalSize = totalSize;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }
}
