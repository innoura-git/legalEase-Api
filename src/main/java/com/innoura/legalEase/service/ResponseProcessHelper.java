package com.innoura.legalEase.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.innoura.legalEase.dto.AiResponseDto;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;


@Service
public class ResponseProcessHelper
{
    public AiResponseDto processAiResponse(String airesponse)
            throws JsonProcessingException
    {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            mapper.configure(JsonParser.Feature.ALLOW_COMMENTS,true);
            return mapper.readValue(airesponse, AiResponseDto.class);
    }

}
