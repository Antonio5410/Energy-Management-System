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

        // 1) trimitem mereu către admin dacă destinația e ADMIN
        if ("ADMIN".equalsIgnoreCase(msg.getTo())) {
            messagingTemplate.convertAndSend("/topic/chat.admin", msg);

            // 2) auto-reply către client (min. 10 reguli)
            String botReply = chatbotService.reply(msg.getContent());
            if (botReply != null) {
                ChatMessage botMsg = new ChatMessage(
                        "BOT",
                        msg.getFromUserId(),
                        botReply
                );

                messagingTemplate.convertAndSend(
                        "/topic/chat.user." + msg.getFromUserId(),
                        botMsg
                );
            }
            return;
        }

        // 3) altfel: admin trimite către user
        messagingTemplate.convertAndSend(
                "/topic/chat.user." + msg.getTo(),
                msg
        );
    }
}
