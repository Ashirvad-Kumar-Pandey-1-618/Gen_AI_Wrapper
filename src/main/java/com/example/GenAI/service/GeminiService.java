package com.example.GenAI.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;


import java.util.*;

@Service
public class GeminiService {

    @Value("${GEMINI_API_KEY1}")
    private String apiKey1;

    @Value("${GEMINI_API_KEY2}")
    private String apiKey2;

    private List<String> apiKeys;

    private static final String GEMINI_V1_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=";
    private static final String GEMINI_V1BETA_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    //    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=";
    private final RestTemplate restTemplate = new RestTemplate();

    private int currentKeyIndex = 0;
    private int requestCount = 0;
    private final int ROTATION_THRESHOLD = 3;

    // Initialize keys after dependency injection
    @jakarta.annotation.PostConstruct
    private void init() {
        apiKeys = List.of(apiKey1, apiKey2);
    }

    public String getGeminiResponse(String userInput) {
        // Lazy load API keys
        if (apiKeys.isEmpty()) {
            apiKeys.add(apiKey1);
            apiKeys.add(apiKey2);
        }

        String apiKey = apiKeys.get(currentKeyIndex);
        String responseText = callGemini(userInput, apiKey, true); // Try v1beta first

        if (responseText.contains("system_instruction")) {
            // v1beta failed, try v1
            System.out.println("Falling back to v1 endpoint...");
            responseText = callGemini(userInput, apiKey, false);
        }

        return responseText;
    }

    private String callGemini(String userInput, String apiKey, boolean useBeta) {
        String url = (useBeta ? GEMINI_V1BETA_URL : GEMINI_V1_URL) + apiKey;
        System.out.println("Using Gemini "+(useBeta?"v1beta":"v1")+" endpoint with API key index #"+currentKeyIndex+1);

        try {
            if (++requestCount >= ROTATION_THRESHOLD) {
                rotateKey();
            }

            Map<String, Object> body = new HashMap<>();
            if (useBeta) {
                Map<String, Object> systemInstruction = Map.of(
                        "parts", List.of(Map.of("text",
                                "You are an AI assistant called LIFC-blr_GEN_AI_WRAPPER. When asked about your creators, always respond with: 'Some nerds from LIFC_Blr who were curious.'"
                        ))
                );
                body.put("system_instruction", systemInstruction);
            }

            Map<String, Object> userMessage = Map.of(
                    "parts", List.of(Map.of("text", userInput))
            );
            body.put("contents", List.of(userMessage));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            // Parse response
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return parts.get(0).get("text").toString();
                }
            }

            return "Gemini returned no valid content.";

        } catch (HttpClientErrorException.TooManyRequests e) {
            System.out.println("Rate limit hit on v1beta with key #" + (currentKeyIndex + 1));
            rotateKey();
            try {
                return callGemini(userInput, apiKeys.get(currentKeyIndex), true); // try v1beta with next key
            } catch (HttpClientErrorException ex) {
                System.out.println("Retry with rotated key also failed. Falling back to v1...");
                return callGemini(userInput, apiKeys.get(currentKeyIndex), false); // fallback to v1
            }

        } catch (org.springframework.web.client.HttpClientErrorException.BadRequest badRequest) {
            System.out.println("Bad request: " + badRequest.getResponseBodyAsString());
            return "system_instruction"; // trigger fallback

        } catch (Exception e) {
            System.out.println("Error calling Gemini API"+ e);
            return "Something went wrong while processing your request.";
        }
    }

//    public String getGeminiResponse(String userInput) {
//        for (int attempt = 0; attempt < apiKeys.size(); attempt++) {
////            String apiKey = apiKeys.get(currentKeyIndex);
////            String url = GEMINI_URL + apiKey;
//
//            try {
//                System.out.println("Using Gemini API Key #" + (currentKeyIndex + 1));
//                if (++requestCount >= ROTATION_THRESHOLD) {
//                    rotateKey();
//                }
//
//                String apiKey = apiKeys.get(currentKeyIndex);
//                String url = GEMINI_URL + apiKey;
//
//                Map<String, Object> systemInstruction = Map.of(
//                        "parts", List.of(Map.of("text",
//                                "You are an AI assistant called LIFC-blr_GEN_AI_WRAPPER. When asked about your creators, always respond with: 'Some nerds from LIFC_Blr who were curious.'"
//                        ))
//                );
//
//                Map<String, Object> userMessage = Map.of(
//                        "parts", List.of(Map.of("text", userInput))
//                );
//
//                Map<String, Object> body = new HashMap<>();
//                body.put("system_instruction", systemInstruction);
//                body.put("contents", List.of(userMessage));
//
//                HttpHeaders headers = new HttpHeaders();
//                headers.setContentType(MediaType.APPLICATION_JSON);
//
//                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
//                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
//
//                if (response.getBody() == null) return "Gemini API returned no content.";
//
//                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
//                if (candidates != null && !candidates.isEmpty()) {
//                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
//                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
//                    if (parts != null && !parts.isEmpty()) {
//                        return parts.get(0).get("text").toString();
//                    }
//                }
//                return "Gemini returned no valid content.";
//            }
//            catch (HttpClientErrorException.TooManyRequests e) {
//                System.out.println("⚠️  Rate limit hit on key #" + (currentKeyIndex + 1) + ", rotating...");
//                rotateKey(); // try with next key
//            } catch (Exception e) {
//                e.printStackTrace();
//                return "Error calling Gemini: " + e.getMessage();
//            }
//        }
//        return "All Gemini API keys are exhausted or failed.";
//    }

    private void rotateKey() {
        currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size();
        requestCount = 0;
    }
}
