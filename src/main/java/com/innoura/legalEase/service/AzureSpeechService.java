package com.innoura.legalEase.service;

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

@Slf4j
@Service
public class AzureSpeechService
{
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;


    public AzureSpeechService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Converts audio file (MP3) to text using Azure Speech-to-Text service
     * @param fileContainer FileContainerDto containing the audio file bytes
     * @return Full text transcription of the audio file
     */
    public String convertSpeechToText(FileContainerDto fileContainer, Prompt prompt) {
        try {
            log.info("Converting speech to text for file: {}", fileContainer.getFileName());

            // Azure Speech-to-Text REST API endpoint
            String apiUrl = String.format("https://%s.stt.speech.microsoft.com/speech/recognition/conversation/cognitiveservices/v1?language=en-US", prompt.getSpeechRegion());

            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/wav"));
            headers.set("Ocp-Apim-Subscription-Key", prompt.getKey());
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

            // Create request entity with audio bytes
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileContainer.getFileByte(), headers);

            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // Parse response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            log.info("Response from ai is : {}",responseJson.toString());

            String transcription = responseJson.path("DisplayText").asText();

            if (transcription == null || transcription.isEmpty()) {
                log.warn("Empty transcription received for file: {}", fileContainer.getFileName());

                log.info("AI Response for type : {} ",prompt.getFileType().name());
                log.info("AI Response {} ",transcription);
            }

            log.info("Successfully converted speech to text for file: {}", fileContainer.getFileName());
            return transcription != null ? transcription : "";

        } catch (Exception e) {
            log.error("Error converting speech to text for file: {}", fileContainer.getFileName(), e);
            throw new RuntimeException("Failed to convert speech to text: " + e.getMessage(), e);
        }
    }
}
