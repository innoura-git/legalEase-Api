package com.innoura.legalEase.dto;

import com.innoura.legalEase.enums.CaseStatus;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
public class CaseReport
{
    private String caseId;
    private String CaseNumber;
    private String title;
    private String client;
    private CaseStatus status;
    private boolean audioCount;
    private boolean excelCount;
    private boolean pdfCount;
    private boolean imageCount;
}
