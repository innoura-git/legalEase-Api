package com.innoura.legalEase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
@AllArgsConstructor
public class AiResponseDto
{
    private String fullContent;
    private String summarizedContent;
}
