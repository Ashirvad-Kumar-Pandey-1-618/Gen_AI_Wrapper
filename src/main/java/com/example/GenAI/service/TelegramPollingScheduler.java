package com.example.GenAI.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TelegramPollingScheduler {

    private final TelegramService telegramService;

    public TelegramPollingScheduler(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    // Check for any new message from Telegram every 5 seconds --> And reply to it with AI generated response
    @Scheduled(fixedRate = 5000)
    public void pollTelegram() {
        telegramService.pollMessagesAndReply();
    }
}

