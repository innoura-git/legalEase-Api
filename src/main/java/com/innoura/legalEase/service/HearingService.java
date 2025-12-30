package com.innoura.legalEase.service;

import com.innoura.legalEase.dbservice.DbService;
import com.innoura.legalEase.dto.CreateHearingDto;
import com.innoura.legalEase.dto.HearingDetailsDto;
import com.innoura.legalEase.entity.CaseDetail;
import com.innoura.legalEase.entity.HearingDetails;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class HearingService
{
    private final DbService dbService;
    private static final String initialHearingName = "Case Filing";

    public HearingService(DbService dbService) {this.dbService = dbService;}

    public HearingDetails createHearing(CreateHearingDto createHearingDto)
    {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setHearingId(UUID.randomUUID().toString());
        hearingDetails.setCaseId(createHearingDto.getCaseId());
        hearingDetails.setHearingName(createHearingDto.getHearingName());
        hearingDetails.setHearingDate(createHearingDto.getHearingDate());
        return dbService.save(hearingDetails, HearingDetails.class.getSimpleName());
    }

    public List<HearingDetailsDto> getHearingDetails(String caseId)
    {

        Query query = new Query(
                Criteria.where(HearingDetails.Fields.caseId).is(caseId)
        );

        query.fields()
                .include(HearingDetails.Fields.caseId)
                .include(HearingDetails.Fields.hearingId)
                .include(HearingDetails.Fields.hearingName)
                .include(HearingDetails.Fields.hearingDate);

        List<HearingDetailsDto> hearingDetailsDtoList =
                dbService.find(query, HearingDetailsDto.class, "HearingDetails");

        List<HearingDetailsDto> sortedHearingDetailsDtoList =
                hearingDetailsDtoList.stream()
                        .sorted(Comparator.comparing(HearingDetailsDto::getHearingDate).reversed())
                        .toList();


        if (hearingDetailsDtoList.isEmpty()) {
            throw new IllegalArgumentException("No Hearing Found for caseId, Hearing should not be empty");
        }
        return sortedHearingDetailsDtoList;
    }

    public HearingDetails createInitialHearing(CaseDetail caseDetail)
    {
        CreateHearingDto createHearingDto = new CreateHearingDto();
        createHearingDto.setCaseId(caseDetail.getCaseId());
        createHearingDto.setHearingName(initialHearingName);
        createHearingDto.setHearingDate(caseDetail.getCaseMetaData().getFilingDate());
        return createHearing(createHearingDto);
    }
}
