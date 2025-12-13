package com.innoura.legalEase.entity;

import com.innoura.legalEase.enums.FileType;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@FieldNameConstants
@Document
public class Prompt
{
    private FileType fileType;
    private String systemPrompt;
    private String messagePrompt;
    private String url;
    private String key;
    private String speechRegion;
}
