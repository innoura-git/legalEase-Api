package com.innoura.legalEase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HearingDetailsDto
{
    private String caseId;
    private String hearingId;
    private String hearingName;
    private LocalDate hearingDate;
}
