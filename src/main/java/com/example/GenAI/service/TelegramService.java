package com.example.GenAI.service;

import jakarta.annotation.PostConstruct;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.example.GenAI.service.GeminiService;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TelegramService {

    @Value("${TELEGRAM_BOT_TOKEN}")
    private String botToken;

    @Value("${TELEGRAM_URL}")
    private String telegramApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private GeminiService geminiService = new GeminiService();
    private final Map<Long, Long> lastHandledUpdates = new ConcurrentHashMap<>();

    private long lastUpdateId = 0;

    @PostConstruct
    public void loadLastUpdateId() {
        File file = new File("last_update_id.txt");
        if (file.exists()) {
            try (Scanner scanner = new Scanner(file)) {
                if (scanner.hasNextLong()) {
                    lastUpdateId = scanner.nextLong();
                    System.out.println("Loaded lastUpdateId from file: " + lastUpdateId);
                }
            } catch (IOException e) {
                System.err.println("Failed to read lastUpdateId file: " + e.getMessage());
            }
        }
    }

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

//This method will check for next Telegram messages every 5 seconds --> Pass to Gemini --> Reply user with Gemini Response;
    public void pollMessagesAndReply() {
        //Building URL for Telegram API to get new updates after last chat;
        String url = telegramApiUrl + botToken + "/getUpdates?timeout=5&offset=" + (lastUpdateId + 1);

        System.out.println("Polling Telegram for updates...\nURL: " + url);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Failed to get updates");
            return;
        }
//Opening received Telegram response packet to fetch chatId and message text;
        try {
            JSONObject json = new JSONObject(response.getBody());
            JSONArray updates = json.getJSONArray("result");

            for (int i = 0; i < updates.length(); i++) {
                JSONObject updateObj = updates.getJSONObject(i);
                long updateId = updateObj.getLong("update_id");

                if (!updateObj.has("message")) continue; // skip if no message
                JSONObject messageObj = updateObj.getJSONObject("message");
                String text = messageObj.getString("text");
                long chatId = messageObj.getJSONObject("chat").getLong("id");

                // Skip if we've already processed this update
                Long lastId = lastHandledUpdates.getOrDefault(chatId, -1L);
                if (updateId <= lastId) {
                    continue;
                }

                System.out.println("Received NEW message: " + text + " from chat ID: " + chatId + "User Name: " + messageObj.getJSONObject("chat").getString("first_name"));

                //For now , we're not using personalities for Gemini - handling common responses --> The persona will be part of next release GenAI-2.0
                if (text.equalsIgnoreCase("start")|| text.equalsIgnoreCase("hi")|| text.equalsIgnoreCase("hello")) {
                    sendMessage(chatId, "Hi! I'm your AI assistant LIFC-blr_GEN_AI_WRAPPER. Ask me anything.");
                } else if (text.equalsIgnoreCase("help")) {
                    sendMessage(chatId, "Send any question or message. Type 'Explain more' to follow up.");
                }
                else {
                    //Calling Gemini to get AI generated response
                        try {
                            // Gemini call
                            String geminiReply = geminiService.getGeminiResponse(text);

                            // Send back to Telegram
                            sendMessageInChunks(chatId, geminiReply);

                            // Update last processed ID
                            lastHandledUpdates.put(chatId, updateId);
                        } catch (Exception geminiEx) {
                            sendMessage(chatId, "Error While Hitting Gemini API");
                            geminiEx.printStackTrace();
                        }
                }
                lastUpdateId = updateId;
                saveLastUpdateIdToFile(lastUpdateId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//If text response is too big (more than 4096 characters), Telegram API will reject it. So we need to send it in parts
    private void sendMessageInChunks(long chatId, String message) {
        int maxLength = 4096;
        int start = 0;

        while (start < message.length()) {
            int end = Math.min(start + maxLength, message.length());
            String chunk = message.substring(start, end);
            sendMessage(chatId, chunk);  // re-use your existing sendMessage()
            start = end;
        }
    }

    private void saveLastUpdateIdToFile(long updateId) {
        try (PrintWriter writer = new PrintWriter("last_update_id.txt")) {
            writer.print(updateId);
            System.out.println("Saved lastUpdateId: " + updateId);
        } catch (IOException e) {
            System.err.println("Failed to write lastUpdateId: " + e.getMessage());
        }
    }




}