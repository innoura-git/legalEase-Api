package com.innoura.legalEase.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CaseHearing
{
    private String caseId;
    private List<HearingDetails> hearingDetails;
}

@Data
class HearingDetails
{

    private String hearingName;
    private LocalDate hearingDate;
    private List<FileMetaData> fileMetaData;
}

@Data
class FileMetaData
{
    private String fileName;
    private String fileId;
}