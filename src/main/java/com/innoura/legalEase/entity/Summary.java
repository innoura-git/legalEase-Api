package com.innoura.legalEase.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Summary
{
    private List<String> summary;
    private List<String> importantPoints;
    private List<String> nextHearing;
    private List<String> ipcSections;
}