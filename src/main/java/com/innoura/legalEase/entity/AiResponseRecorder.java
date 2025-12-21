package com.innoura.legalEase.entity;

import com.innoura.legalEase.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiResponseRecorder
{
    private String caseId;
    private String aiResponse;
    private FileType fileType;
}