package com.innoura.legalEase.dto;

import com.innoura.legalEase.entity.HearingDetails;
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

    public HearingDetailsDto(HearingDetails hearingDetails)
    {
        this.caseId = hearingDetails.getCaseId();
        this.hearingId = hearingDetails.getHearingId();
        this.hearingName = hearingDetails.getHearingName();
        this.hearingDate = hearingDetails.getHearingDate();
    }
}
