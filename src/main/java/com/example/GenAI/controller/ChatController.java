package com.example.GenAI.controller;
import com.example.GenAI.model.RequestModel;
import com.example.GenAI.model.ResponseModel;
import com.example.GenAI.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/genAI")
    public ResponseModel chat(@RequestBody RequestModel request) {
        String aiResponse = geminiService.getGeminiResponse(request.getText());
        return new ResponseModel(aiResponse);
    }
}

