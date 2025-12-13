package com.innoura.legalEase.dto;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;

@Data
@FieldNameConstants
public class CourtInformation
{
    private String court;
    private String presidingJudge;
    private LocalDate nextHiringDate;
}
