package com.example.GenAI.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TelegramPollingScheduler {

    private final TelegramService telegramService;

    public TelegramPollingScheduler(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    // Every 5 seconds
    @Scheduled(fixedRate = 5000)
    public void pollTelegram() {
        try {
            telegramService.pollMessagesAndReply();
        }catch( Exception e) {
            System.out.println("Error during Telegram polling: " + e.getMessage());
        }
    }
}

