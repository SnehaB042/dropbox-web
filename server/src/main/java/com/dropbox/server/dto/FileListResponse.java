package com.dropbox.server.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileListResponse {
    private List<FileMetaDataResponse> files;
    private long numberOfFiles;
    private int currentPage;
    private int totalPages;
    private long totalFiles;

    public FileListResponse() {}
    
    public FileListResponse(List<FileMetaDataResponse> files, long numberOfFiles,
                           int currentPage, int totalPages, long totalFiles) {
        this.files = files;
        this.numberOfFiles = numberOfFiles;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalFiles = totalFiles;
    }
}
