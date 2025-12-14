package com.innoura.legalEase.service;

import com.innoura.legalEase.dbservice.DbService;
import com.innoura.legalEase.entity.ExceptionLog;
import com.innoura.legalEase.entity.FileDetail;
import com.innoura.legalEase.entity.Prompt;
import com.innoura.legalEase.enums.FileType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ChatService
{
    private final DbService dbService;
    private final AiCallService aiCallService;

    public ChatService(DbService dbService, AiCallService aiCallService)
    {
        this.dbService = dbService;
        this.aiCallService = aiCallService;
    }

    public String getAnswer(String caseId, FileType fileType, String question)
    {
        Query query = new Query(Criteria.where(Prompt.Fields.fileType).is(FileType.QA));
        Prompt prompt = dbService.findOne(query, Prompt.class);
        if (prompt == null) {
            ExceptionLog exceptionLog = new ExceptionLog(caseId, "no prompt found in the db for the QA");
            dbService.save(exceptionLog);
            log.error("no prompt found in the db for the QA for caseId :{}", caseId);
            return null;
        }
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where(FileDetail.Fields.caseId).is(caseId));
        criteriaList.add(Criteria.where(FileDetail.Fields.fileType).is(fileType));
        query = new Query(
                new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))
        );
        FileDetail fileDetail = dbService.findOne(query, FileDetail.class);
        if(fileDetail == null || fileDetail.getFullContent() == null)
        {
            ExceptionLog exceptionLog = new ExceptionLog(caseId, "file detail is suspicious for caseId : " + caseId + "fileDetail : " + fileDetail);
            dbService.save(exceptionLog);
            log.error("file detail is suspicious for caseId : {}, fileDetail :{}", caseId, fileDetail);
            return null;
        }

     return  aiCallService.getGptResponse("content" + fileDetail.getFullContent() + "\n Question : \n" +question, prompt, caseId);
    }
}
