package com.example.demo.realtimesupportservice.ws;

import com.example.demo.realtimesupportservice.dto.ChatMessage;
import com.example.demo.realtimesupportservice.service.ChatbotService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWsController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatbotService chatbotService;

    public ChatWsController(SimpMessagingTemplate messagingTemplate, ChatbotService chatbotService) {
        this.messagingTemplate = messagingTemplate;
        this.chatbotService = chatbotService;
    }

    @MessageMapping("/chat.send")
    public void handleChat(ChatMessage msg) {

        // 1) client -> admin
        if ("ADMIN".equalsIgnoreCase(msg.getTo())) {
            messagingTemplate.convertAndSend("/topic/chat.admin", msg);

            // 2) chatbot: răspunde doar dacă are regulă clară
            String botReply = chatbotService.reply(msg.getContent());
            if (botReply != null && !botReply.isBlank()) {
                ChatMessage botMsg = new ChatMessage("BOT", msg.getFromUserId(), botReply);

                messagingTemplate.convertAndSend(
                        "/topic/chat.user." + msg.getFromUserId(),
                        botMsg
                );
            }

            return;
        }

        // 3) admin -> client (to = userId)
        messagingTemplate.convertAndSend("/topic/chat.user." + msg.getTo(), msg);
    }
}
