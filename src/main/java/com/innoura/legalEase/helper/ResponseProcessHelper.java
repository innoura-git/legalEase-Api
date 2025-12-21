package com.innoura.legalEase.helper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innoura.legalEase.entity.Summary;
import org.springframework.stereotype.Service;


@Service
public class ResponseProcessHelper
{
    public Summary processAiResponse(String airesponse)
            throws JsonProcessingException
    {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            mapper.configure(JsonParser.Feature.ALLOW_COMMENTS,true);
            return mapper.readValue(airesponse, Summary.class);
    }

}
