package com.dropbox.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "file_metadata", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "original_filename", "duplicate_sequence" })})
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "display_filename", nullable = false)
    private String displayFilename;

    @Column(name = "stored_filename", unique = true, nullable = false)
    private String storedFilename;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "file_extension", nullable = false, length = 10)
    private String fileExtension;

    @Column(name = "upload_timestamp", nullable = false)
    private LocalDateTime uploadTimestamp;

    @Column(name = "checksum", length = 64)
    private String checksum;

    @Column(name = "duplicate_sequence", nullable = false)
    private Integer duplicateSequence = 0;

    public FileMetadata() {}
    
    public FileMetadata(String originalFilename, String displayFilename, String storedFilename,
                     String filePath, Long fileSize, String mimeType, String fileExtension,
                     String checksum, Integer duplicateSequence) {
        this.originalFilename = originalFilename;
        this.displayFilename = displayFilename;
        this.storedFilename = storedFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
        this.checksum = checksum;
        this.duplicateSequence = duplicateSequence;
        this.uploadTimestamp = LocalDateTime.now();
    }
}
