package com.innoura.legalEase.service;

import com.innoura.legalEase.dbservice.DbService;
import com.innoura.legalEase.dto.FileContainerDto;
import com.innoura.legalEase.entity.ExceptionLog;
import com.innoura.legalEase.entity.FileDetail;
import com.innoura.legalEase.entity.Prompt;
import com.innoura.legalEase.enums.FileType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ChatService
{
    private final DbService dbService;
    private final AiCallService aiCallService;

    public ChatService(DbService dbService, AiCallService aiCallService)
    {
        this.dbService = dbService;
        this.aiCallService = aiCallService;
    }

    public String getAnswer(String caseId, FileType fileType, String question, String hearingId)
            throws Exception
    {
        Query query = new Query(Criteria.where(Prompt.Fields.fileType).is(FileType.QA));
        Prompt prompt = dbService.findOne(query, Prompt.class);
        if (prompt == null) {
            ExceptionLog exceptionLog = new ExceptionLog(caseId, "no prompt found in the db for the QA");
            dbService.save(exceptionLog);
            log.error("no prompt found in the db for the QA for caseId :{}", caseId);
            return null;
        }
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where(FileDetail.Fields.caseId).is(caseId));
        criteriaList.add(Criteria.where(FileDetail.Fields.fileType).is(fileType));
        criteriaList.add(Criteria.where(FileDetail.Fields.hearingId).is(hearingId));
        query = new Query(
                new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))
        );
        FileDetail fileDetail = dbService.findOne(query, FileDetail.class);
        if(fileDetail == null)
        {
            ExceptionLog exceptionLog = new ExceptionLog(caseId, "file detail is suspicious for caseId : " + caseId + "fileDetail : " + fileDetail);
            dbService.save(exceptionLog);
            log.error("file detail is suspicious for caseId : {}, fileDetail :{}", caseId, fileDetail);
            return null;
        }

        if (fileType.name().equals(FileType.IMAGE.name()))
        {
            FileContainerDto fileContainerDto = getFileForDownload(caseId,fileType);
            return aiCallService.getImageResponse(fileContainerDto,prompt,question, fileDetail.getFilePath(),fileDetail.getMimeType());
        }
        return aiCallService.getGptResponse("content" + fileDetail.getFullContent() + "\n Question : \n" + question, prompt, caseId);
    }

    public FileContainerDto getFileForDownload(String caseId, FileType fileType) {
        log.info("Getting file for download - caseId: {}, fileType: {}", caseId, fileType);

        Query query = new Query(
                Criteria.where(FileDetail.Fields.caseId).is(caseId)
                        .and(FileDetail.Fields.fileType).is(fileType)
        );

        FileDetail fileDetail =
                dbService.findOne(query, FileDetail.class, FileDetail.class.getSimpleName());

        if (fileDetail == null) {
            log.error("FileDetail not found for caseId: {}, fileType: {}", caseId, fileType);
            throw new IllegalArgumentException(
                    "FileDetail not found for caseId: " + caseId + ", fileType: " + fileType
            );
        }

        String filePath = fileDetail.getFilePath();
        if (filePath == null || filePath.isBlank()) {
            log.error("File path not found in FileDetail for caseId: {}, fileType: {}", caseId, fileType);
            throw new IllegalArgumentException(
                    "File path not found for caseId: " + caseId + ", fileType: " + fileType
            );
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            log.error("File does not exist at path: {}", filePath);
            throw new IllegalArgumentException("File does not exist at path: " + filePath);
        }

        try {
            byte[] fileBytes = Files.readAllBytes(path);

            FileContainerDto dto = new FileContainerDto();
            dto.setCaseId(caseId);
            dto.setFileName(path.getFileName().toString());
            dto.setFileByte(fileBytes);

            log.info("Successfully loaded file into memory - filename: {}, size: {} bytes",
                    dto.getFileName(), fileBytes.length);

            return dto;

        } catch (IOException e) {
            log.error("Failed to read file bytes for path: {}", filePath, e);
            throw new RuntimeException("Failed to read file for download", e);
        }
    }

}
