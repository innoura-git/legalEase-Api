package com.innoura.legalEase.dto;

import com.innoura.legalEase.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetaData
{
    private String fileId;
    private String fileName;
    private FileType fileType;
    private String filePath;
}
