package com.dropbox.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Component
@ConfigurationProperties(prefix = "file.storage")
@Getter
@Setter
public class FileStorageProperties {

    private String location = "./uploads";
    private long maxSize = 52428800; // 50MB -> convert to 1 GB
    private Set<String> allowedTypes = Set.of("txt", "jpg", "jpeg", "png", "json", "pdf");
    private int maxDuplicatesPerName = 999;
    
}
