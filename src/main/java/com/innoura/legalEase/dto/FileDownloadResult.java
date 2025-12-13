package com.innoura.legalEase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@AllArgsConstructor
public class FileDownloadResult {
    private Resource resource;
    private String contentType;
    private String filename;
}
