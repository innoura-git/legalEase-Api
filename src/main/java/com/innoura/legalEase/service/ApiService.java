package com.innoura.legalEase.service;

import com.innoura.legalEase.dbservice.DbService;
import com.innoura.legalEase.dto.CaseReport;
import com.innoura.legalEase.dto.EvidenceDto;
import com.innoura.legalEase.dto.FileDownloadResult;
import com.innoura.legalEase.dto.HistoryDto;
import com.innoura.legalEase.entity.CaseDetail;
import com.innoura.legalEase.entity.FileDetail;
import com.innoura.legalEase.entity.HearingDetails;
import com.innoura.legalEase.entity.Summary;
import com.innoura.legalEase.enums.FileType;
import com.innoura.legalEase.helper.ApiHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ApiService
{
    private static final String hearingNameHeader = "Hearing Name : ";
    private static final String hearingDateHeader = "Hearing Date : ";
    private static final String evidenceTypeHeader = "Evidence Type : ";
    private final DbService dbService;
    private final ApiHelper apiHelper;

    public ApiService(DbService dbService, ApiHelper apiHelper)
    {
        this.dbService = dbService;
        this.apiHelper = apiHelper;
    }

    public List<CaseReport> getAllCaseReport()
    {
        Query query = new Query()
                .with(Sort.by(Sort.Direction.DESC, "createdDate"));

        List<CaseDetail> caseDetails = dbService.find(query,CaseDetail.class);
        
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

    public FileDownloadResult getFileForDownload(String caseId, FileType fileType, String hearingId)
    {
        log.info("Getting file for download - caseId: {}, fileType: {}", caseId, fileType);

        Query query = new Query(Criteria.where(FileDetail.Fields.caseId).is(caseId)
                .and(FileDetail.Fields.fileType).is(fileType).and(FileDetail.Fields.hearingId).is(hearingId));
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
    
    private String getContentTypeByFileType(FileType fileType) {
        return switch (fileType) {
            case PDF -> "application/pdf";
            case EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case AUDIO -> "audio/mpeg";
            case IMAGE -> "image/jpeg";
            default -> null;
        };
    }

    public String getSummaryForFile(String caseId, FileType fileType, String hearingId)
    {
        Query query = new Query(
                Criteria.where(FileDetail.Fields.caseId).is(caseId)
                        .and(FileDetail.Fields.fileType).is(fileType)
                        .and(FileDetail.Fields.hearingId).is(hearingId)
        );

        FileDetail fileDetail = dbService.findOne(
                query,
                FileDetail.class,
                FileDetail.class.getSimpleName()
        );

        if (fileDetail == null || fileDetail.getSummarizedContent() == null) {
            return "<p>No summary available.</p>";
        }

        Summary summary = fileDetail.getSummarizedContent();

        StringBuilder html = new StringBuilder();

        apiHelper.appendSection(html, "Summary", summary.getSummary());
        apiHelper.appendSection(html, "Important Points", summary.getImportantPoints());
        apiHelper.appendSection(html, "Next Hearing", summary.getNextHearing());
        apiHelper.appendSection(html, "IPC Sections", summary.getIpcSections());

        return !html.isEmpty() ? html.toString() : "<p>No summary available.</p>";
    }

    public List<HistoryDto> getHistorySections(String caseId)
    {

        // 1️⃣ Fetch hearings
        Query hearingQuery = new Query(
                Criteria.where(HearingDetails.Fields.caseId).is(caseId)
        );
        List<HearingDetails> hearingDetailsList =
                dbService.find(hearingQuery, HearingDetails.class);

        // 2️⃣ Extract hearingIds
        List<String> hearingIds = hearingDetailsList.stream()
                .map(HearingDetails::getHearingId)
                .filter(Objects::nonNull)
                .toList();

        log.info("Hearing Id : {}",hearingIds.toString());
        // 3️⃣ Fetch all files for those hearings
        Query fileDetailQuery = new Query(
                Criteria.where(FileDetail.Fields.hearingId).in(hearingIds)
        );

        List<FileDetail> fileDetailList =
                dbService.find(fileDetailQuery, FileDetail.class);

        // 4️⃣ Group files by hearingId
        Map<String, List<FileDetail>> filesByHearingId =
                fileDetailList.stream()
                        .filter(fd -> fd.getHearingId() != null)
                        .collect(Collectors.groupingBy(FileDetail::getHearingId));

        List<HistoryDto> historyDtoList = new ArrayList<>();
        for (HearingDetails hearing : hearingDetailsList) {

            HistoryDto historyDto = new HistoryDto();
            historyDto.setHearingName(hearing.getHearingName());
            historyDto.setHearingDate(hearing.getHearingDate());
            historyDto.setEvidenceDtoList(new ArrayList<>());

            List<FileDetail> files =
                    filesByHearingId.getOrDefault(
                            hearing.getHearingId(),
                            Collections.emptyList()
                    );

            for (FileDetail file : files) {

                if (file.getSummarizedContent() == null) {
                    continue;
                }
                if (file.getSummarizedContent().getIpcSections() == null || file.getSummarizedContent().getIpcSections().isEmpty()) {
                    continue;
                }

                String evidenceName = apiHelper.getEvidenceType(file.getFileType());
                EvidenceDto evidenceDto = new EvidenceDto();
                evidenceDto.setEvidenceName(evidenceName);
                evidenceDto.setIpcSections(file.getSummarizedContent().getIpcSections());
                historyDto.getEvidenceDtoList().add(evidenceDto);
            }
            historyDtoList.add(historyDto);
        }
        return historyDtoList;
    }



}
