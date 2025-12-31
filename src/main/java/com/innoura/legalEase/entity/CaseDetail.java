package com.innoura.legalEase.entity;

import com.innoura.legalEase.dto.CaseMetaData;
import com.innoura.legalEase.dto.CourtInformation;
import com.innoura.legalEase.dto.LegalReferences;
import com.innoura.legalEase.dto.PartiesInvolved;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Data
@FieldNameConstants
@Document
public class CaseDetail
{
    @Id
    private String id;
    private String caseId;
    private CaseMetaData caseMetaData;
    private CourtInformation courtInformation;
    private PartiesInvolved partiesInvolved;
    private LegalReferences legalReferences;
    private String audioFilePaths;
    private String pdfFilePaths;
    private String excelFilePaths;
    private String imageFilePaths;
    private Instant createdDate;
}
