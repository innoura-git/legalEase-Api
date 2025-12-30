package com.innoura.legalEase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvidenceDto
{
    private String evidenceName;
    private List<String> ipcSections;
}
