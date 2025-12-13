package com.innoura.legalEase.utils;

import com.innoura.legalEase.dto.FileContainerDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileContainerUtils
{
    public FileContainerDto constructFileContainer(MultipartFile file, String caseId) throws IOException
    {
        FileContainerDto fileContainer = new FileContainerDto();
        
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
