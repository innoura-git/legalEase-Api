package com.innoura.legalEase.controller;

import com.innoura.legalEase.dto.CreateHearingDto;
import com.innoura.legalEase.dto.HearingDetailsDto;
import com.innoura.legalEase.entity.HearingDetails;
import com.innoura.legalEase.service.HearingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/hearing")
public class HearingController
{
    private final HearingService hearingService;

    public HearingController(HearingService hearingService) {this.hearingService = hearingService;}

    @PostMapping("/create")
    public ResponseEntity<?> createHearing(@RequestBody CreateHearingDto createHearingDto)
    {
        try {
            if (createHearingDto.getCaseId() == null) {
                throw new IllegalArgumentException("CaseId cannot be null");
            }
            HearingDetails hearingDetails = hearingService.createHearing(createHearingDto);
            HearingDetailsDto hearingDetailsDto = new HearingDetailsDto(hearingDetails);
            return ResponseEntity.ok(hearingDetailsDto);
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @GetMapping("/get")
    public ResponseEntity<?> getHearingList(@RequestParam("caseId") String caseId)
    {
        try {
            if (caseId == null || caseId.isEmpty()) {
                throw new IllegalArgumentException("CaseId cannot be null");
            }
            List<HearingDetailsDto> hearingDetailsList = hearingService.getHearingDetails(caseId);
            return ResponseEntity.ok(hearingDetailsList);
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
