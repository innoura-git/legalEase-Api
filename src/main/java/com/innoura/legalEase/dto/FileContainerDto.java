package com.innoura.legalEase.dto;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
public class FileContainerDto
{
    private String caseId;
    private String fileName;
    private byte[] fileByte;
}
