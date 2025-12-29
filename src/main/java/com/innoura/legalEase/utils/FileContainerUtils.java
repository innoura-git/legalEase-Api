package com.innoura.legalEase.utils;

import com.innoura.legalEase.dto.FileContainerDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileContainerUtils
{
    public FileContainerDto constructFileContainer(MultipartFile file, String caseId, String hearingId)
            throws IOException
    {
        FileContainerDto fileContainer = new FileContainerDto();
        fileContainer.setFileId(UUID.randomUUID().toString());
        fileContainer.setHearingId(hearingId);
        // Set the file name
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            fileName = file.getName();
        }
        fileContainer.setFileName(fileName);
        
        // Set the file content as byte array
        byte[] fileBytes = file.getBytes();
        fileContainer.setFileByte(fileBytes);
        fileContainer.setCaseId(caseId);
        
        return fileContainer;
    }
}
