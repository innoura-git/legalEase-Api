package com.innoura.legalEase.entity;

import com.innoura.legalEase.dto.FileMetaData;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@Document
@NoArgsConstructor
@FieldNameConstants
public class HearingDetails
{
    @Id
    private String id;
    private String caseId;
    private String hearingId;
    private String hearingName;
    private LocalDate hearingDate;
    private List<FileMetaData> fileMetaData;
}