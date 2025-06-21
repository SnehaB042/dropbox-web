package com.dropbox.server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dropbox.server.model.FileMetadata;

@Repository
public interface FileRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findByOriginalFilenameOrderByDuplicateSequenceDesc(String originalFilename);
    
    boolean existsByOriginalFilenameAndDuplicateSequence(String originalFilename, Integer sequence);
    
    @Query("SELECT f FROM FileMetadata f ORDER BY f.uploadTimestamp DESC")
    Page<FileMetadata> findAllOrderByUploadTimeDesc(Pageable pageable);
    
    Optional<FileMetadata> findByChecksum(String checksum);
    
    @Query("SELECT COUNT(f), COALESCE(SUM(f.fileSize), 0) FROM FileMetadata f")
    Object[] getFileStatistics();
}
