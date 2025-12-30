package com.innoura.legalEase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryDto
{
    private String hearingName;
    private LocalDate hearingDate;
    private List<EvidenceDto> evidenceDtoList;
}
