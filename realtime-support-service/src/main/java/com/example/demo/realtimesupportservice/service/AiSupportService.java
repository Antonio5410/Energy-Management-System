package com.example.demo.realtimesupportservice.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class AiSupportService {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper om;

    // ✅ IMPORTANT: default "" ca să nu crape dacă nu există proprietatea
    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.model:gpt-5-mini}")
    private String model;

    public AiSupportService(ObjectMapper om) {
        this.om = om;
    }

    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String suggestReply(String customerMessage) {
        // 🧯 SAFE MODE: rulează fără cheie
        if (!isEnabled()) {
            return "AI Support is currently unavailable (missing OPENAI key).";
        }

        try {
            String input = """
                You are a customer support assistant for an Energy Management System.
                Provide a short, practical answer in Romanian.
                If you are not sure, say you need more details and propose 1-2 clarifying questions.

                Customer message: %s
                """.formatted(customerMessage);

            String json = """
                {
                  "model": "%s",
                  "input": %s,
                  "text": { "verbosity": "low" },
                  "reasoning": { "effort": "none" }
                }
                """.formatted(model, om.writeValueAsString(input));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/responses"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() < 200 || res.statusCode() >= 300) {
                return "AI error: HTTP " + res.statusCode();
            }

            JsonNode root = om.readTree(res.body());

            JsonNode outText = root.get("output_text");
            if (outText != null && !outText.asText().isBlank()) {
                return outText.asText().trim();
            }

            return "AI: Nu am putut extrage un răspuns (format neașteptat).";

        } catch (Exception e) {
            return "AI exception: " + e.getMessage();
        }
    }
}
