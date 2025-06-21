package com.dropbox.server.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileMetaDataResponse {
    private Long id;
    private String displayFilename;
    private String originalFilename;
    private Long fileSize;
    private String mimeType;
    private String fileExtension;
    private LocalDateTime uploadTimestamp;
    private Integer duplicateSequence;
    private String checksum;    

    public FileMetaDataResponse() {}
    
    public FileMetaDataResponse(Long id, String displayFilename, String originalFilename,
                               Long fileSize, String mimeType, String fileExtension,
                               LocalDateTime uploadTimestamp,
                               Integer duplicateSequence, String checksum) {
        this.id = id;
        this.displayFilename = displayFilename;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
        this.uploadTimestamp = uploadTimestamp;
        this.duplicateSequence = duplicateSequence;
        this.checksum = checksum;
    }
}
