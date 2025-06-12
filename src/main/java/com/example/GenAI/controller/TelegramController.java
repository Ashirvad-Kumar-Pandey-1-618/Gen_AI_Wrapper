package com.example.GenAI.controller;

import com.example.GenAI.service.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/telegram")
public class TelegramController {

    @Autowired
    private TelegramService telegramService;

    @PostMapping("/sendMessage")
    public String sendMsg(@RequestParam Long chatId, @RequestParam String message) {
        telegramService.sendMessage(chatId, message);
        return "Message sent!";
    }

    @GetMapping("/poll")
    public String poll() {
        telegramService.pollMessagesAndReply();
        return "Polling done!";
    }
}

