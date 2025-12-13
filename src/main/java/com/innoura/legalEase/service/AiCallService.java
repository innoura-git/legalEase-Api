package com.innoura.legalEase.service;

import com.innoura.legalEase.dto.AiResponseDto;
import com.innoura.legalEase.dto.FileContainerDto;
import com.innoura.legalEase.entity.Prompt;
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
    private final ResponseProcessHelper responseProcessHelper;


    public AiCallService(RestTemplate restTemplate, ObjectMapper objectMapper, ResponseProcessHelper responseProcessHelper)
    {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.responseProcessHelper = responseProcessHelper;
    }

    public String getGptResponse(String content, Prompt prompt)
    {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", prompt.getKey());

                Map<String, Object> body = new HashMap<>();

                body.put("model", "gpt-5-mini");

                // messages list
                List<Map<String, String>> messages = new ArrayList<>();
                messages.add(Map.of(
                        "role", "system",
                        "content", prompt.getSystemPrompt()
                ));
                messages.add(Map.of(
                        "role", "user",
                        "content", content
                ));
                body.put("messages", messages);

                // reasoning effort
                Map<String, Object> reasoning = new HashMap<>();
                reasoning.put("effort", "high");  // deeper reasoning
                body.put("reasoning", reasoning);

                // verbosity control to make output more detailed
                Map<String, Object> textParams = new HashMap<>();
                textParams.put("verbosity", "high");
                body.put("text", textParams);

                // optional: max output tokens
                body.put("max_output_tokens", 128000);


                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        prompt.getUrl(),
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                );

                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choicesNode = root.path("choices");
                JsonNode firstChoice = choicesNode.get(0);
                JsonNode messageNode = firstChoice.path("message");

                JsonNode contentNode = messageNode.path("content");
                log.info("AI Response for type : {} ",prompt.getFileType().name());
                log.info("AI Response {} ",contentNode.asText());

                return contentNode.asText();
            }
            catch (Exception e) {
                retryCount++;
                if (retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return "Request interrupted";
                    }
                }
                else {
                    return "Failed to get response after retries. Error: " + e.getMessage();
                }
            }
        }
        return "Failed to get response after retries.";
    }

    public AiResponseDto getImageResponse(FileContainerDto fileContainer, Prompt prompt, String filePath)
            throws Exception
    {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {

                log.info("Entering image processing for image : {} for caseId : {}", fileContainer.getFileName(), fileContainer.getCaseId());

                byte[] imageBytes = Files.readAllBytes(Path.of(filePath));
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                String dataUrl = "data:image/jpeg;base64," + base64Image;

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", prompt.getKey());

                Map<String, Object> body = new HashMap<>();
                body.put("model", "gpt-5-mini");  // or another vision-capable model

                // messages: system + user with embedded image part
                List<Map<String, Object>> messages = new ArrayList<>();

                messages.add(Map.of(
                        "role", "system",
                        "content", prompt.getSystemPrompt()
                ));

                List<Map<String, Object>> userContent = new ArrayList<>();
                userContent.add(Map.of(
                        "type", "image_url",
                        "image_url", dataUrl
                ));

                messages.add(Map.of(
                        "role", "user",
                        "content", userContent
                ));

                body.put("messages", messages);

                // you can still include reasoning verbosity if desired
                Map<String, Object> reasoning = new HashMap<>();
                reasoning.put("effort", "high");
                body.put("reasoning", reasoning);

                // verbosity control to make output more detailed
                Map<String, Object> textParams = new HashMap<>();
                textParams.put("verbosity", "high");
                body.put("text", textParams);

                // optional: max output tokens
                body.put("max_output_tokens", 128000);

                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        prompt.getUrl(),
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                );

                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choicesNode = root.path("choices");
                JsonNode firstChoice = choicesNode.get(0);
                JsonNode messageNode = firstChoice.path("message");

                JsonNode contentNode = messageNode.path("content");

                log.info("AI Response for type : {} ",prompt.getFileType().name());
                log.info("AI Response {} ",contentNode.asText());

                AiResponseDto aiResponseDto = responseProcessHelper.processAiResponse(contentNode.toString());
                return aiResponseDto;
            }
            catch (Exception e) {
                retryCount++;
                if (retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return new AiResponseDto("Error in AI Response","Error in AI Response");
                    }
                }
                else {
                    throw new Exception(e.getMessage());
                }
            }
            log.info("Exiting image processing for image : {}", fileContainer.getFileName());
        }
        return new AiResponseDto("Error in AI Response","Error in AI Response");
    }
}
