package com.innoura.legalEase.service;

import com.innoura.legalEase.dbservice.DbService;
import com.innoura.legalEase.dto.FileContainerDto;
import com.innoura.legalEase.entity.AiResponseRecorder;
import com.innoura.legalEase.entity.ExceptionLog;
import com.innoura.legalEase.entity.Prompt;
import com.innoura.legalEase.entity.Summary;
import com.innoura.legalEase.enums.FileType;
import com.innoura.legalEase.helper.ResponseProcessHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AiCallService
{
    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 10000;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final DbService dbService;
    private final ResponseProcessHelper responseProcessHelper;


    public AiCallService(RestTemplate restTemplate, ObjectMapper objectMapper, DbService dbService, ResponseProcessHelper responseProcessHelper)
    {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.dbService = dbService;
        this.responseProcessHelper = responseProcessHelper;
    }

    public String getGptResponse(String content, Prompt prompt, String caseId) {
        log.info("Entering file process for : {}", prompt.getFileType());
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", prompt.getKey());

                Map<String, Object> body = new HashMap<>();

                // ---- CHAT COMPLETIONS PAYLOAD ----
                List<Map<String, String>> messages = new ArrayList<>();

                if (prompt.getFileType().name().equals("AUDIO"))
                {
                    log.info("System prompt : {}",prompt.getSystemPrompt());
                    log.info("User prompt : {}",content);
                }
                messages.add(Map.of(
                        "role", "system",
                        "content", prompt.getSystemPrompt()
                ));

                messages.add(Map.of(
                        "role", "user",
                        "content", content
                ));

                body.put("messages", messages);

                body.put("max_completion_tokens", 128000);

                HttpEntity<Map<String, Object>> requestEntity =
                        new HttpEntity<>(body, headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        prompt.getUrl(),   // .../chat/completions
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                );

                JsonNode root = objectMapper.readTree(response.getBody());

                // ---- STANDARD CHAT COMPLETIONS READ ----
                String result = root
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();
                aiResponseSave(result,caseId,prompt.getFileType());
                return result;
            }
            catch (Exception e) {
                retryCount++;
                if (retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return "Request interrupted";
                    }
                } else {
                    ExceptionLog exceptionLog = new ExceptionLog(caseId,e.getMessage());
                    dbService.save(exceptionLog);
                    return "";
                }
            }
        }
        return "";
    }



    public String getImageResponse(
            FileContainerDto fileContainer,
            Prompt prompt,
            String filePath,
            String mimeType) throws Exception
    {
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try {
                log.info("Entering image processing for image : {} for caseId : {}",
                        fileContainer.getFileName(), fileContainer.getCaseId());

                // --- Read image ---
                byte[] imageBytes = Files.readAllBytes(Path.of(filePath));
                String base64Image = Base64.getEncoder()
                        .encodeToString(imageBytes)
                        .replaceAll("\\s+", "");

                String dataUrl = "data:"+mimeType+";base64," + base64Image;

                // --- Headers (same as WebClient) ---
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", prompt.getKey());

                // --------------------------------------------------
                // REQUEST BODY (MATCHES WORKING WEBCLIENT VERSION)
                // --------------------------------------------------

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model","gpt-5-mini");

                List<Map<String, Object>> messages = new ArrayList<>();

                Map<String, Object> userMessage = new HashMap<>();
                userMessage.put("role", "user");

                List<Map<String, Object>> content = new ArrayList<>();

                // text block
                content.add(Map.of(
                        "type", "text",
                        "text", prompt.getSystemPrompt()
                ));

                // image block
                content.add(Map.of(
                        "type", "image_url",
                        "image_url", Map.of(
                                "url", dataUrl
                        )
                ));

                userMessage.put("content", content);
                messages.add(userMessage);

                requestBody.put("messages", messages);
                requestBody.put("max_completion_tokens", 128000);
                requestBody.put("reasoning_effort","low");
                requestBody.put("stream",false);

                HttpEntity<Map<String, Object>> requestEntity =
                        new HttpEntity<>(requestBody, headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        prompt.getUrl(),   // chat/completions endpoint
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                );

                // --- Parse response (unchanged) ---
                JsonNode root = objectMapper.readTree(response.getBody());
                String result = root
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();

                aiResponseSave(result,fileContainer.getCaseId(),prompt.getFileType());
                return result;
            }
            catch (Exception e) {
                retryCount++;
                if (retryCount < MAX_RETRIES) {
                    Thread.sleep(RETRY_DELAY_MS);
                } else {
                    ExceptionLog exceptionLog = new ExceptionLog(fileContainer.getCaseId(),e.getMessage());
                    dbService.save(exceptionLog);
                    return "";
                }
            }
            finally {
                log.info("Exiting image processing for image : {}", fileContainer.getFileName());
            }
        }

        return "";
    }

    public Summary getTextSummary(String content, Prompt prompt, String caseId)
    {
        log.info("Entering file process for : {}", prompt.getFileType());
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", prompt.getKey());

                Map<String, Object> body = new HashMap<>();

                // ---- CHAT COMPLETIONS PAYLOAD ----
                List<Map<String, String>> messages = new ArrayList<>();

                if (prompt.getFileType().name().equals("AUDIO")) {
                    log.info("System prompt : {}", prompt.getSystemPrompt());
                    log.info("User prompt : {}", content);
                }
                messages.add(Map.of(
                        "role", "system",
                        "content", prompt.getSystemPrompt()
                ));

                messages.add(Map.of(
                        "role", "user",
                        "content", content
                ));

                body.put("messages", messages);

                body.put("max_completion_tokens", 128000);

                HttpEntity<Map<String, Object>> requestEntity =
                        new HttpEntity<>(body, headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        prompt.getUrl(),   // .../chat/completions
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                );

                JsonNode root = objectMapper.readTree(response.getBody());

                // ---- STANDARD CHAT COMPLETIONS READ ----
                String result = root
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();
                aiResponseSave(result, caseId, prompt.getFileType());
                return responseProcessHelper.processAiResponse(result);
            }
            catch (Exception e) {
                retryCount++;
                if (retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return new Summary(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                    }
                }
                else {
                    ExceptionLog exceptionLog = new ExceptionLog(caseId, e.getMessage());
                    dbService.save(exceptionLog);
                    return new Summary(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                }
            }
        }
        return new Summary(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public Summary getImageSummary(
            FileContainerDto fileContainer,
            Prompt prompt,
            String mimeType)
            throws Exception
    {
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try {
                log.info("Entering image processing for image : {} for caseId : {}",
                        fileContainer.getFileName(), fileContainer.getCaseId());

                // --- Read image ---
                byte[] imageBytes = fileContainer.getFileByte();
                String base64Image = Base64.getEncoder()
                        .encodeToString(imageBytes)
                        .replaceAll("\\s+", "");

                String dataUrl = "data:" + mimeType + ";base64," + base64Image;

                // --- Headers (same as WebClient) ---
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", prompt.getKey());

                // --------------------------------------------------
                // REQUEST BODY (MATCHES WORKING WEBCLIENT VERSION)
                // --------------------------------------------------

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", "gpt-5-mini");

                List<Map<String, Object>> messages = new ArrayList<>();

                Map<String, Object> userMessage = new HashMap<>();
                userMessage.put("role", "user");

                List<Map<String, Object>> content = new ArrayList<>();

                // text block
                content.add(Map.of(
                        "type", "text",
                        "text", prompt.getSystemPrompt()
                ));

                // image block
                content.add(Map.of(
                        "type", "image_url",
                        "image_url", Map.of(
                                "url", dataUrl
                        )
                ));

                userMessage.put("content", content);
                messages.add(userMessage);

                requestBody.put("messages", messages);
                requestBody.put("max_completion_tokens", 128000);
                requestBody.put("reasoning_effort", "low");
                requestBody.put("stream", false);

                HttpEntity<Map<String, Object>> requestEntity =
                        new HttpEntity<>(requestBody, headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        prompt.getUrl(),   // chat/completions endpoint
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                );

                // --- Parse response (unchanged) ---
                JsonNode root = objectMapper.readTree(response.getBody());
                String result = root
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();
                log.info("Image summary response : {}", result);
                aiResponseSave(result, fileContainer.getCaseId(), prompt.getFileType());
                log.info("Image summary response : {}", result);
                return responseProcessHelper.processAiResponse(result);
            }
            catch (Exception e) {
                retryCount++;
                if (retryCount < MAX_RETRIES) {
                    Thread.sleep(RETRY_DELAY_MS);
                }
                else {
                    ExceptionLog exceptionLog = new ExceptionLog(fileContainer.getCaseId(), e.getMessage());
                    dbService.save(exceptionLog);
                    return new Summary(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                }
            }
            finally {
                log.info("Exiting image processing for image : {}", fileContainer.getFileName());
            }
        }

        return new Summary(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    private void aiResponseSave(String aiResponse,String caseId, FileType fileType)
    {
        AiResponseRecorder aiResponseRecorder = new AiResponseRecorder(caseId,aiResponse,fileType);
        dbService.save(aiResponseRecorder);
        log.info("Saved Ai Response for file type : {} and caseId : {}", fileType.name(), caseId);
    }

}
