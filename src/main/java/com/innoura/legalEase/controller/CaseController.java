package com.innoura.legalEase.controller;

import com.innoura.legalEase.dbservice.DbService;
import com.innoura.legalEase.dto.CaseReport;
import com.innoura.legalEase.dto.FileDownloadResult;
import com.innoura.legalEase.entity.CaseDetail;
import com.innoura.legalEase.entity.ExceptionLog;
import com.innoura.legalEase.enums.FileType;
import com.innoura.legalEase.service.ApiService;
import com.innoura.legalEase.service.FileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/case")
public class CaseController
{
    private final DbService dbService;
    private final ApiService apiService;
    private final FileProcessService fileProcessService;

    public CaseController(DbService dbService, ApiService apiService, FileProcessService fileProcessService) {
        this.dbService = dbService;
        this.apiService = apiService;
        this.fileProcessService = fileProcessService;
    }

    @PostMapping("/create")
    public ResponseEntity<CaseDetail> saveCaseDetail(@RequestBody CaseDetail caseDetail)
    {
        try{
            caseDetail.setCaseId(UUID.randomUUID().toString());
            log.info("Saving case detail for patientId: {}", caseDetail.getCaseId());
            CaseDetail savedCaseDetail = dbService.save(caseDetail);
            log.info("Successfully saved case detail for patientId: {}", savedCaseDetail.getCaseId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCaseDetail);
        }
        catch (Exception e) {
            log.error("exception occurred while saving new caseDetail ", e);
            ExceptionLog exceptionLog = new ExceptionLog(caseDetail.getCaseId(), e.getMessage());
            dbService.save(exceptionLog);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
            fileProcessService.saveFiles(caseId, excelFile, audioFile, image, pdfFile);
            return ResponseEntity.ok("Files uploaded successfully for caseId: " + caseId);
        }
        catch (Exception e) {
            log.error("Exception occurred while uploading files for caseId: {}", caseId, e);
            ExceptionLog exceptionLog = new ExceptionLog(caseId, e.getMessage());
            dbService.save(exceptionLog);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading files: " + e.getMessage());
        }
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<CaseReport>> getAllCaseReport()
    {
        try{
            log.info("Fetching all case reports");
            List<CaseReport> caseReports = apiService.getAllCaseReport();
            log.info("Successfully fetched {} case reports", caseReports.size());
            return ResponseEntity.ok(caseReports);
        }
        catch (Exception e) {
            log.error("Exception occurred while fetching all case reports", e);
            ExceptionLog exceptionLog = new ExceptionLog("", e.getMessage());
            dbService.save(exceptionLog);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download/file")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam("caseId") String caseId,
            @RequestParam("fileType") FileType fileType)
    {
        try {
            log.info("Downloading file for caseId: {}, fileType: {}", caseId, fileType);
            
            FileDownloadResult fileDownloadResult = apiService.getFileForDownload(caseId, fileType);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileDownloadResult.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDownloadResult.getFilename() + "\"")
                    .body(fileDownloadResult.getResource());
        }
        catch (IllegalArgumentException e) {
            log.error("Invalid request for file download - caseId: {}, fileType: {}", caseId, fileType, e);
            ExceptionLog exceptionLog = new ExceptionLog(caseId, e.getMessage());
            dbService.save(exceptionLog);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        catch (Exception e) {
            log.error("Exception occurred while downloading file for caseId: {}, fileType: {}", caseId, fileType, e);
            ExceptionLog exceptionLog = new ExceptionLog(caseId, e.getMessage());
            dbService.save(exceptionLog);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
