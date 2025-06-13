package com.example.GenAI.service;
import com.example.GenAI.model.RequestModel;
import com.example.GenAI.model.ResponseModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class GeminiService {

    @Value("${GEMINI_API_KEY}")
    private String apiKey;


    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=";
    private final RestTemplate restTemplate = new RestTemplate();

    public String getGeminiResponse(String userInput) {
        String url = GEMINI_URL+apiKey;

        System.out.println("Hitting :"+url+" \nKey :"+apiKey);

        // Gemini API request format
        Map<String, Object> message = new HashMap<>();
        message.put("parts", List.of(Map.of("text", userInput)));

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(message));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        try {
            // Parse nested structure: candidates[0].content.parts[0].text
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return parts.get(0).get("text").toString();
                }
            }
        } catch (Exception e) {
            return "Failed to parse Gemini response: " + e.getMessage();
        }

        return "No valid response from Gemini.";
    }
}

