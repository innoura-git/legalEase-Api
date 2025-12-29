package com.innoura.legalEase.controller;

import com.innoura.legalEase.enums.FileType;
import com.innoura.legalEase.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AiChatController
{
    private final ChatService chatService;

    public AiChatController(ChatService chatService) {this.chatService = chatService;}

    @PostMapping("/chat")
    public ResponseEntity<String> getAiChat(@RequestParam("caseId") String caseId, @RequestParam("fileType")FileType fileType, @RequestBody String question)
            throws Exception
    {

        String response = chatService.getAnswer(caseId,fileType,question);
        return ResponseEntity.ok(response);
    }
}
