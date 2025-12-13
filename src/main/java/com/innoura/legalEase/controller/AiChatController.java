package com.innoura.legalEase.controller;

import com.innoura.legalEase.enums.FileType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AiChatController
{
    @GetMapping("/chat")
    public ResponseEntity<String> getAiChat(@RequestParam("caseId") String caseId, @RequestParam("fileType")FileType fileType, @RequestBody String question)
    {
        return null;

    }

}
