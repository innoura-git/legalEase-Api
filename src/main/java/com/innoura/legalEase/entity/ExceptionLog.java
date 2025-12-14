package com.innoura.legalEase.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionLog
{
    private String caseId;
    private String exceptionMessage;
}
