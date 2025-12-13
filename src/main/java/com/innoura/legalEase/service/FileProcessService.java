package com.innoura.legalEase.service;

import com.innoura.legalEase.dbservice.DbService;
import com.innoura.legalEase.dto.AiResponseDto;
import com.innoura.legalEase.dto.FileContainerDto;
import com.innoura.legalEase.entity.CaseDetail;
import com.innoura.legalEase.entity.FileDetail;
import com.innoura.legalEase.entity.Prompt;
import com.innoura.legalEase.enums.FileType;
import com.innoura.legalEase.utils.FileContainerUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.json.JsonParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class FileProcessService
{
    private final DbService dbService;
    private final FileContainerUtils fileContainerUtils;
    private final ContentExtractionService contentExtractionService;
    private final AiCallService aiCallService;
    private final ResponseProcessHelper responseProcessHelper;
    private final AzureSpeechService azureSpeechService;

    @Value("${file.upload.path}")
    private String fileUploadPath;

    public FileProcessService(DbService dbService, FileContainerUtils fileContainerUtils, ContentExtractionService contentExtractionService, AiCallService aiCallService, ResponseProcessHelper responseProcessHelper,AzureSpeechService azureSpeechService)
    {
        this.dbService = dbService;
        this.fileContainerUtils = fileContainerUtils;
        this.contentExtractionService = contentExtractionService;
        this.aiCallService = aiCallService;
        this.responseProcessHelper = responseProcessHelper;
        this.azureSpeechService = azureSpeechService;
    }

    public void saveFiles(String caseId, MultipartFile excelFile, MultipartFile audioFile, 
                         MultipartFile image, MultipartFile pdfFile) throws IOException {
        log.info("Uploading files for caseId: {}", caseId);

        // Find the CaseDetail by caseId (patientId)
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
            String excelFilePath = saveFile(excelFile, caseId, "excel", uploadPath);
            caseDetail.setExcelFilePaths(excelFilePath);
            log.info("Saved excel file: {}", excelFilePath);
        }

        if (audioFile != null && !audioFile.isEmpty()) {
            String audioFilePath = saveFile(audioFile, caseId, "audio", uploadPath);
            caseDetail.setAudioFilePaths(audioFilePath);
            log.info("Saved audio file: {}", audioFilePath);
        }

        if (image != null && !image.isEmpty()) {
            String imageFilePath = saveFile(image, caseId, "image", uploadPath);
            caseDetail.setImageFilePaths(imageFilePath);
            log.info("Saved image file: {}", imageFilePath);
        }

        if (pdfFile != null && !pdfFile.isEmpty()) {
            String pdfFilePath = saveFile(pdfFile, caseId, "pdf", uploadPath);
            caseDetail.setPdfFilePaths(pdfFilePath);
            log.info("Saved pdf file: {}", pdfFilePath);
        }

        // Update CaseDetail in database
        dbService.save(caseDetail);
        log.info("Successfully updated CaseDetail for caseId: {}", caseId);
        // Construct file containers and process them asynchronously using parallel streams
        try {
            String audioFilePath = null;
            String pdfFilePath = null;
            String imageFilePath = null;
            String excelFilePath = null;

            FileContainerDto audioFileContainer = null;
            FileContainerDto pdfFileContainer = null;
            FileContainerDto imageFileContainer = null;
            FileContainerDto excelFileContainer = null;

            if (audioFile != null && !audioFile.isEmpty()) {
                audioFileContainer = fileContainerUtils.constructFileContainer(audioFile, caseId);
                audioFilePath = caseDetail.getAudioFilePaths();
            }
            if (pdfFile != null && !pdfFile.isEmpty()) {
                pdfFileContainer = fileContainerUtils.constructFileContainer(pdfFile, caseId);
                pdfFilePath = caseDetail.getPdfFilePaths();
            }
            if (image != null && !image.isEmpty()) {
                imageFileContainer = fileContainerUtils.constructFileContainer(image, caseId);
                imageFilePath = caseDetail.getImageFilePaths();
            }
            if (excelFile != null && !excelFile.isEmpty()) {
                excelFileContainer = fileContainerUtils.constructFileContainer(excelFile, caseId);
                excelFilePath = caseDetail.getExcelFilePaths();
            }

            processAsyncFile(pdfFileContainer, pdfFilePath, excelFileContainer, excelFilePath,
                    audioFileContainer, audioFilePath, imageFileContainer, imageFilePath);
        } catch (Exception e) {
            log.error("Error constructing file containers for caseId: {}", caseId, e);
        }
    }

    private String saveFile(MultipartFile file, String caseId, String fileType, Path uploadPath) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = fileType + ".file";
        }

        String filename = caseId + "_" + fileType + "_" + originalFilename;
        Path filePath = uploadPath.resolve(filename);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    private void processAsyncFile(FileContainerDto pdfFile, String pdfFilePath,
            FileContainerDto excelFile, String excelFilePath,
            FileContainerDto audioFile, String audioFilePath,
            FileContainerDto imageFile, String imageFilePath)
    {
        // Create a list of file containers with their types and paths
        List<FileContainerWrapper> fileContainers = Arrays.asList(
                new FileContainerWrapper("pdf", pdfFile, pdfFilePath),
                new FileContainerWrapper("excel", excelFile, excelFilePath),
                new FileContainerWrapper("audio", audioFile, audioFilePath),
                new FileContainerWrapper("image", imageFile, imageFilePath)
        );

        // Process files in parallel using parallel stream
        fileContainers.parallelStream()
            .filter(wrapper -> wrapper.fileContainer != null)
            .forEach(wrapper -> {
                try {
                    processFile(wrapper.fileType, wrapper.fileContainer, wrapper.filePath);
                } catch (Exception e) {
                    log.error("Error processing {} file: {}", wrapper.fileType, e.getMessage(), e);
                }
            });
        log.info("file process completed !");
    }

    private void processFile(String fileType, FileContainerDto fileContainer, String filePath)
    {
        try {
            switch (fileType) {
                case "pdf":
                    processPdfFile(fileContainer, filePath);
                    break;
                case "excel":
                    processExcelFile(fileContainer, filePath);
                    break;
                case "audio":
                    processAudioFile(fileContainer, filePath);
                    break;
                case "image":
                    processImageFile(fileContainer, filePath);
                    break;
                default:
                    log.warn("Unknown file type: {}", fileType);
            }
        } catch (Exception e) {
            log.error("Error processing {} file", fileType, e);
        }
    }

    private static class FileContainerWrapper {
        String fileType;
        FileContainerDto fileContainer;
        String filePath;

        FileContainerWrapper(String fileType, FileContainerDto fileContainer, String filePath)
        {
            this.fileType = fileType;
            this.fileContainer = fileContainer;
            this.filePath = filePath;
        }
    }

    private void processPdfFile(FileContainerDto fileContainer, String filePath)
            throws IOException
    {
        log.info("Processing PDF file: {}", fileContainer.getFileName());
        String pdfFileContent = contentExtractionService.extractPdfAsString(fileContainer);
//        Query query = new Query(Criteria.where(Prompt.Fields.fileType).is(FileType.PDF));
//        Prompt prompt = dbService.findOne(query, Prompt.class);
//        String summarizedContent = aiCallService.getGptResponse(pdfFileContent,prompt);
        FileDetail fileDetail = new FileDetail();
        fileDetail.setCaseId(fileContainer.getCaseId())
                .setFilePath(filePath)
                .setFileType(FileType.PDF)
                .setFullContent(pdfFileContent);
                //.setSummarizedContent(summarizedContent);
        dbService.save(fileDetail, FileDetail.class.getSimpleName());
        log.info("pdf file content saved for the caseId :{}", fileContainer.getCaseId());
    }

    private void processExcelFile(FileContainerDto fileContainer, String filePath) throws IOException {
        log.info("Processing Excel file: {}", fileContainer.getFileName());
        String excelFileContent = contentExtractionService.extractExcelAsString(fileContainer);
//        Query query = new Query(Criteria.where(Prompt.Fields.fileType).is(FileType.EXCEL));
//        Prompt prompt = dbService.findOne(query, Prompt.class);
//        String summarizedContent = aiCallService.getGptResponse(excelFileContent, prompt);
        FileDetail fileDetail = new FileDetail();
        fileDetail.setCaseId(fileContainer.getCaseId())
                .setFilePath(filePath)
                .setFileType(FileType.EXCEL)
                .setFullContent(excelFileContent);
               // .setSummarizedContent(summarizedContent);
        dbService.save(fileDetail, FileDetail.class.getSimpleName());
        log.info("excel file content saved for the caseId :{}", fileContainer.getCaseId());
    }

    private void processAudioFile(FileContainerDto fileContainer, String filePath) {
        log.info("Processing Audio file: {}", fileContainer.getFileName());

        Query query = new Query(Criteria.where(Prompt.Fields.fileType).is(FileType.AUDIO));
        Prompt prompt = dbService.findOne(query, Prompt.class);

        String fullMp3Content = azureSpeechService.convertSpeechToText(fileContainer,prompt);
        String summarizedContent = aiCallService.getGptResponse(fullMp3Content, prompt);
        FileDetail fileDetail = new FileDetail();
        fileDetail.setCaseId(fileContainer.getCaseId())
                .setFilePath(filePath)
                .setFileType(FileType.AUDIO);
               // .setFullContent(fullMp3Content)
               // .setSummarizedContent(summarizedContent);
        dbService.save(fileDetail, FileDetail.class.getSimpleName());
    }

    private void processImageFile(FileContainerDto fileContainer, String filePath)
            throws Exception
    {
        log.info("Processing image file : {}",fileContainer.getFileName());
        Query query = new Query(Criteria.where(Prompt.Fields.fileType).is(FileType.IMAGE));
        Prompt prompt = dbService.findOne(query, Prompt.class);
        AiResponseDto aiResponseDto =aiCallService.getImageResponse(fileContainer,prompt,filePath);
        FileDetail fileDetail = new FileDetail();
        fileDetail.setCaseId(fileContainer.getCaseId())
                .setFilePath(filePath)
                .setFileType(FileType.IMAGE);
               // .setFullContent(aiResponseDto.getFullContent())
               // .setSummarizedContent(aiResponseDto.getSummarizedContent());
        dbService.save(fileDetail, FileDetail.class.getSimpleName());
        log.info("Processing Image file: {}", fileContainer.getFileName());
    }
}
