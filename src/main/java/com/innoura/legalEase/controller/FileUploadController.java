package com.innoura.legalEase.controller;

import com.innoura.legalEase.service.MachineUploadHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping("/machine")
public class FileUploadController
{
    private final MachineUploadHelper machineUploadHelper;

    public FileUploadController(MachineUploadHelper machineUploadHelper) {
        this.machineUploadHelper = machineUploadHelper;
        }

    @PostMapping("/upload/files")
    public ResponseEntity<String> saveFiles(
            @RequestParam("caseId") String caseId,
            @RequestParam(value = "excelFile", required = false) MultipartFile excelFile,
            @RequestParam(value = "audioFile", required = false) MultipartFile audioFile,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile)
    {
        try {
            machineUploadHelper.uploadFileToMachine(caseId, excelFile, audioFile, image, pdfFile);
            return ResponseEntity.ok("Files uploaded successfully for caseId: " + caseId);
        }
        catch (Exception e) {
            log.error("Exception occurred while uploading files for caseId: {}", caseId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading files: " + e.getMessage());
        }
    }
}
