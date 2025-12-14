package com.innoura.legalEase.service;

import com.innoura.legalEase.dbservice.DbService;
import com.innoura.legalEase.dto.CaseReport;
import com.innoura.legalEase.dto.FileDownloadResult;
import com.innoura.legalEase.entity.CaseDetail;
import com.innoura.legalEase.entity.FileDetail;
import com.innoura.legalEase.enums.FileType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ApiService
{
    private final DbService dbService;

    public ApiService(DbService dbService)
    {
        this.dbService = dbService;
    }

    public List<CaseReport> getAllCaseReport()
    {
        List<CaseDetail> caseDetails = dbService.findAll(CaseDetail.class);
        
        return caseDetails.stream()
                .map(this::mapToCaseReport)
                .collect(Collectors.toList());
    }

    private CaseReport mapToCaseReport(CaseDetail caseDetail)
    {
        CaseReport report = new CaseReport();
        
        // Extract data from CaseDetail
        if (caseDetail.getCaseMetaData() != null) {
            report.setCaseNumber(caseDetail.getCaseMetaData().getCaseNumber());
            report.setTitle(caseDetail.getCaseMetaData().getCaseTitle());
        }
        
        // Set client from partiesInvolved (using petitioner as client)
        if (caseDetail.getPartiesInvolved() != null) {
            report.setClient(caseDetail.getPartiesInvolved().getPetitioner());
        }
        
        // Check if file path fields are present (not null and not empty)
        report.setAudioCount(caseDetail.getAudioFilePaths() != null && !caseDetail.getAudioFilePaths().isEmpty());
        report.setExcelCount(caseDetail.getExcelFilePaths() != null && !caseDetail.getExcelFilePaths().isEmpty());
        report.setPdfCount(caseDetail.getPdfFilePaths() != null && !caseDetail.getPdfFilePaths().isEmpty());
        report.setImageCount(caseDetail.getImageFilePaths() != null && !caseDetail.getImageFilePaths().isEmpty());
        report.setCaseId(caseDetail.getCaseId());
        
        report.setStatus(null);
        
        return report;
    }

    public FileDownloadResult getFileForDownload(String caseId, FileType fileType) {
        log.info("Getting file for download - caseId: {}, fileType: {}", caseId, fileType);

        Query query = new Query(Criteria.where(FileDetail.Fields.caseId).is(caseId)
                .and(FileDetail.Fields.fileType).is(fileType));
        FileDetail fileDetail = dbService.findOne(query, FileDetail.class, FileDetail.class.getSimpleName());

        if (fileDetail == null) {
            log.error("FileDetail not found for caseId: {}, fileType: {}", caseId, fileType);
            throw new IllegalArgumentException("FileDetail not found for caseId: " + caseId + ", fileType: " + fileType);
        }
        
        // Get file path from FileDetail
        String filePath = fileDetail.getFilePath();
        if (filePath == null || filePath.isEmpty()) {
            log.error("File path not found in FileDetail for caseId: {}, fileType: {}", caseId, fileType);
            throw new IllegalArgumentException("File path not found for caseId: " + caseId + ", fileType: " + fileType);
        }
        
        // Read file from local filesystem
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            log.error("File does not exist at path: {}", filePath);
            throw new IllegalArgumentException("File does not exist at path: " + filePath);
        }
        
        Resource resource = new FileSystemResource(path);
        
        // Determine content type
        String contentType;
        try {
            contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = getContentTypeByFileType(fileType);
            }
        } catch (Exception e) {
            log.warn("Could not probe content type, using default for fileType: {}", fileType, e);
            contentType = getContentTypeByFileType(fileType);
        }
        
        // Get filename from path
        String filename = path.getFileName().toString();
        
        log.info("Successfully prepared file for download - filename: {}, contentType: {}", filename, contentType);
        return new FileDownloadResult(resource, contentType, filename);
    }
    
    private String getFilePathByType(CaseDetail caseDetail, FileType fileType) {
        return switch (fileType) {
            case PDF -> caseDetail.getPdfFilePaths();
            case EXCEL -> caseDetail.getExcelFilePaths();
            case AUDIO -> caseDetail.getAudioFilePaths();
            case IMAGE -> caseDetail.getImageFilePaths();
            default -> null;
        };
    }
    
    private String getContentTypeByFileType(FileType fileType) {
        return switch (fileType) {
            case PDF -> "application/pdf";
            case EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case AUDIO -> "audio/mpeg";
            case IMAGE -> "image/jpeg";
            default -> null;
        };
    }

    public String getSummaryForFile(String caseId, FileType fileType) {
        Query query = new Query(
                Criteria.where(FileDetail.Fields.caseId).is(caseId)
                        .and(FileDetail.Fields.fileType).is(fileType)
        );

        FileDetail fileDetail = dbService.findOne(query, FileDetail.class, FileDetail.class.getSimpleName());
        String content = fileDetail.getSummarizedContent();

        if (content == null || content.trim().isEmpty()) {
            return "<p>No summary available.</p>";
        }

        // Split into sentences and convert to HTML list items
        String listItems = Arrays.stream(content.split("\\."))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.replaceFirst("^[-–—]\\s*", "")) // remove leading -, – or —
                .map(s -> "<li>" + s + ".</li>")             // add period & wrap in <li>
                .collect(Collectors.joining("\n"));

        // Wrap in <ul>
        return "<ul>" + listItems + "</ul>";

    }

}
