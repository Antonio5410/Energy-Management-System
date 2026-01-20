package com.example.demo.realtimesupportservice.ws;

import com.example.demo.realtimesupportservice.service.AiSupportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost")
@RestController
@RequestMapping("/realtime/ai")
public class AiSupportController {

    private final AiSupportService ai;

    public AiSupportController(AiSupportService ai) {
        this.ai = ai;
    }

    @PostMapping("/suggest")
    public ResponseEntity<?> suggest(@RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "").trim();
        if (message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "message is required"));
        }

        String suggestion = ai.suggestReply(message);

        // dacă AI e indisponibil, întoarcem sugestie goală (admin răspunde manual)
        if (suggestion == null || suggestion.isBlank() || suggestion.toLowerCase().contains("unavailable")) {
            return ResponseEntity.ok(Map.of("suggestion", ""));
        }

        return ResponseEntity.ok(Map.of("suggestion", suggestion));
    }
}
