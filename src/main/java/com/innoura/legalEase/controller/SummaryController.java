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
public class SummaryController {

    private final ApiService apiService;

    public SummaryController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping(value = "/get/summary", produces = "text/html; charset=UTF-8")
    public ResponseEntity<String> getSummaryForFile(
            @RequestParam("caseId") String caseId,
            @RequestParam("fileType") FileType fileType
    ) {
        String summaryHtml = apiService.getSummaryForFile(caseId, fileType);

        // If the service already returns HTML, keep it as-is.
        // Otherwise wrap it in simple HTML tags:
        String htmlPage = """
            <html>
                <head>
                    <title>Summary</title>
                </head>
                <body>
                    %s
                </body>
            </html>
            """.formatted(summaryHtml);

        return ResponseEntity.ok(htmlPage);
    }
}

