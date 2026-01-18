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
    public void send(ChatMessage msg) {
        if (msg == null || msg.getFromUserId() == null || msg.getFromUserId().isBlank()) return;
        if (msg.getTo() == null || msg.getTo().isBlank()) return;

        // dacă user trimite către ADMIN => încercăm chatbot
        if ("ADMIN".equalsIgnoreCase(msg.getTo())) {
            String autoReply = chatbotService.tryReply(msg.getContent());

            if (autoReply != null) {
                // răspuns automat către user
                ChatMessage reply = new ChatMessage("ADMIN-BOT", msg.getFromUserId(), autoReply);
                messagingTemplate.convertAndSend("/topic/chat.user." + msg.getFromUserId(), reply);
                return;
            }

            // dacă nu avem regulă => forward la admin (uman)
            messagingTemplate.convertAndSend("/topic/chat.admin", msg);
            return;
        }

        // dacă e mesaj admin -> user sau user -> user
        messagingTemplate.convertAndSend("/topic/chat.user." + msg.getTo(), msg);
    }
}
