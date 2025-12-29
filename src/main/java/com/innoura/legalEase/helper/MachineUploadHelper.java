package com.innoura.legalEase.helper;

import com.innoura.legalEase.dbservice.DbService;
import com.innoura.legalEase.entity.CaseDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class MachineUploadHelper
{
    private final DbService dbService;
    @Value("${file.upload.path}")
    private String fileUploadPath;

    public MachineUploadHelper(DbService dbService) {this.dbService = dbService;}

    public void uploadFileToMachine(String caseId, MultipartFile excelFile, MultipartFile audioFile,
            MultipartFile image, MultipartFile pdfFile, String hearingId)
            throws IOException
    {
        CaseDetail caseDetail = dbService.findById(caseId, CaseDetail.class);
        if (caseDetail == null) {
            log.error("CaseDetail not found for caseId: {}", caseId);
            throw new IllegalArgumentException("CaseDetail not found for caseId: " + caseId);
        }

        Path uploadPath = Paths.get(fileUploadPath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created directory: {}", fileUploadPath);
        }

        if (excelFile != null && !excelFile.isEmpty()) {
            String excelFilePath = saveFile(excelFile, caseId, "excel", uploadPath, hearingId);
            log.info("Saved excel file: {}", excelFilePath);
        }

        if (audioFile != null && !audioFile.isEmpty()) {
            String audioFilePath = saveFile(audioFile, caseId, "audio", uploadPath, hearingId);
            log.info("Saved audio file: {}", audioFilePath);
        }

        if (image != null && !image.isEmpty()) {
            String imageFilePath = saveFile(image, caseId, "image", uploadPath, hearingId);
            log.info("Saved image file: {}", imageFilePath);
        }

        if (pdfFile != null && !pdfFile.isEmpty()) {
            String pdfFilePath = saveFile(pdfFile, caseId, "pdf", uploadPath, hearingId);
            log.info("Saved pdf file: {}", pdfFilePath);
        }
    }

    private String saveFile(MultipartFile file, String caseId, String fileType, Path uploadPath, String hearingId)
            throws IOException
    {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = fileType + ".file";
        }

        String filename = caseId + "_" + hearingId + "_" + fileType + "_" + originalFilename;
        Path filePath = uploadPath.resolve(filename);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }
}
