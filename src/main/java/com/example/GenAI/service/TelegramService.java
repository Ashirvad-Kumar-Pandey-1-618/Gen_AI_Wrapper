package com.example.GenAI.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.example.GenAI.service.GeminiService;

import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramService {

    @Value("${TELEGRAM_BOT_TOKEN}")
    private String botToken;

    @Value("${TELEGRAM_URL}")
    private String telegramApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private GeminiService geminiService = new GeminiService();

    public TelegramService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public void sendMessage(Long chatId, String message) {
        String url = telegramApiUrl + botToken + "/sendMessage";

        Map<String, String> body = new HashMap<>();
        body.put("chat_id", String.valueOf(chatId));
        body.put("text", message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void pollMessagesAndReply() {
        String url = telegramApiUrl + botToken + "/getUpdates";
        System.out.println("Polling Telegram for updates...\nURL: " + url);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Failed to get updates");
            return;
        }

        long chatId = 0;
        try {
            JSONObject json = new JSONObject(response.getBody());
            JSONArray updates = json.getJSONArray("result");

            for (int i = 0; i < updates.length(); i++) {
                JSONObject messageObj = updates.getJSONObject(i).getJSONObject("message");
                String text = messageObj.getString("text");
                chatId = messageObj.getJSONObject("chat").getLong("id");
                System.out.println("Received message: " + text + " from chat ID: " + chatId);

                // Gemini call
                String geminiReply = geminiService.getGeminiResponse(text);

                // Send back to Telegram
                sendMessage(chatId, geminiReply);
            }
        } catch (Exception e) {
            sendMessage(chatId, "Error While Hitting Gemini API");
            e.printStackTrace();
        }
    }


}