package com.innoura.legalEase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
@AllArgsConstructor
@NoArgsConstructor
public class AiResponseDto
{
    private String fullContent;
    private String summarizedContent;
}
