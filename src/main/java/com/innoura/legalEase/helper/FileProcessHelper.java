package com.innoura.legalEase.helper;

import com.innoura.legalEase.dto.FileContainerDto;
import com.innoura.legalEase.dto.FileMetaData;
import com.innoura.legalEase.enums.FileType;
import org.springframework.stereotype.Service;

@Service
public class FileProcessHelper
{
    public FileMetaData createFileMetadata(FileContainerDto fileContainerDto, String filePath, FileType fileType)
    {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setFileId(fileContainerDto.getFileId());
        fileMetaData.setFileName(fileContainerDto.getFileName());
        fileMetaData.setFilePath(filePath);
        fileMetaData.setFileType(fileType);
        return fileMetaData;
    }
}
