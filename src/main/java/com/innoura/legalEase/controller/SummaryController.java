package com.innoura.legalEase.controller;

import com.innoura.legalEase.dbservice.DbService;
import com.innoura.legalEase.entity.ExceptionLog;
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
    private final DbService dbService;

    public SummaryController(ApiService apiService, DbService dbService) {
        this.apiService = apiService;
        this.dbService = dbService;
    }

    @GetMapping(value = "/get/summary", produces = "text/html; charset=UTF-8")
    public ResponseEntity<String> getSummaryForFile(
            @RequestParam("caseId") String caseId,
            @RequestParam("fileType") FileType fileType
    ) {
        try {
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
        catch (Exception e)
        {
            ExceptionLog exceptionLog = new ExceptionLog(caseId, e.getMessage());
            dbService.save(exceptionLog);
        }
        return ResponseEntity.ok("");
    }
}

