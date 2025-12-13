package com.innoura.legalEase.dto;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
public class LegalReferences
{
    private String actsInvolved;
    private String sections;
    private String caseDescription;
}
