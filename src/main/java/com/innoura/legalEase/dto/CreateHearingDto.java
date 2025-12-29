package com.innoura.legalEase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateHearingDto
{
    private String caseId;
    private String hearingName;
    private LocalDate hearingDate;
}
