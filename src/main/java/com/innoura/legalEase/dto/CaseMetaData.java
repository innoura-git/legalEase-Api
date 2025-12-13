package com.innoura.legalEase.dto;

import com.innoura.legalEase.enums.CaseType;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;

@Data
@FieldNameConstants
public class CaseMetaData
{
    private String caseNumber;
    private CaseType caseType;
    private String CaseTitle;
    private LocalDate filingDate;
}
