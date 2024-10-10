package com.f0rsaken.imageprocessor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
@Data
@ConfigurationProperties(prefix = "file")
public class FileConfig {

    private List<String> allowedExtensions;
    private List<String> allowedMimeTypes;
    private long maxFileSize;
    private File tempDir;

}
