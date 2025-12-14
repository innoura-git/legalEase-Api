package com.innoura.legalEase.controller;

import com.innoura.legalEase.enums.FileType;
import com.innoura.legalEase.service.ApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/file")
public class SummaryController
{
    private final ApiService apiService;

    public SummaryController(ApiService apiService) {this.apiService = apiService;}

    @GetMapping("/get/summary")
    public ResponseEntity<String> getSummaryForFile(@RequestParam("caseId") String caseId,
            @RequestParam("fileType") FileType fileType)
    {
        String summaryForFile = apiService.getSummaryForFile(caseId,fileType);
        return ResponseEntity.ok(summaryForFile);
    }
}
