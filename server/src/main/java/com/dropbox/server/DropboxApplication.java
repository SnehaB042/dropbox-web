package com.dropbox.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.dropbox.server.config.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class)
public class DropboxApplication {
    public static void main(String[] args) {
        SpringApplication.run(DropboxApplication.class, args);
    }
}
