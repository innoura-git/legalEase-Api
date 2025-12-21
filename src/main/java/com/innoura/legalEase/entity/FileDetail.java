package com.innoura.legalEase.entity;

import com.innoura.legalEase.enums.FileType;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@FieldNameConstants
@Accessors(chain = true)
public class FileDetail
{
    @Id
    private String id;
    private String caseId;
    private FileType fileType;
    private String fullContent;
    private Summary summarizedContent;
    private String filePath;
}
