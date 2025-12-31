package com.innoura.legalEase.controller;

import com.innoura.legalEase.dbservice.DbService;
import com.innoura.legalEase.dto.HistoryDto;
import com.innoura.legalEase.entity.ExceptionLog;
import com.innoura.legalEase.service.ApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/history")
public class HistoryController
{
    private final ApiService apiService;
    private final DbService dbService;

    public HistoryController(ApiService apiService, DbService dbService)
    {
        this.apiService = apiService;
        this.dbService = dbService;
    }

    @GetMapping("/get")
    public ResponseEntity<?> getSummaryForFile(
            @RequestParam("caseId") String caseId
    )
    {
        try {
            List<HistoryDto> historyDtoList = apiService.getHistorySections(caseId);
            return ResponseEntity.ok(historyDtoList);
        }
        catch (Exception e) {
            ExceptionLog exceptionLog = new ExceptionLog(caseId, e.getMessage());
            dbService.save(exceptionLog);
        }
        return ResponseEntity.ok("");
    }
}
