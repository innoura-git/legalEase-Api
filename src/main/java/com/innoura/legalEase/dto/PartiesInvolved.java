package com.innoura.legalEase.dto;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
public class PartiesInvolved
{
    private String petitioner;
    private String defendant;
    private String RepresentingLawyer;

}
