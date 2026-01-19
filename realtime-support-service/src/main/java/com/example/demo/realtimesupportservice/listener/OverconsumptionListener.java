package com.example.demo.realtimesupportservice.listener;

import com.example.demo.realtimesupportservice.config.RabbitConfig;
import com.example.demo.realtimesupportservice.dto.ChatMessage;
import com.example.demo.realtimesupportservice.dto.OverconsumptionAlert;
import tools.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class OverconsumptionListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public OverconsumptionListener(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitConfig.OVERCONSUMPTION_ALERTS_QUEUE)
    public void handleAlert(String body) {
        try {
            OverconsumptionAlert alert = objectMapper.readValue(body, OverconsumptionAlert.class);

            String text = String.format(
                    "⚠️ Overconsumption detected! Device %s: %.2f > %.2f",
                    alert.getDeviceId(),
                    alert.getValue(),
                    alert.getMaxAllowed()
            );

            ChatMessage msg = new ChatMessage("SYSTEM", alert.getUserId(), text);

            messagingTemplate.convertAndSend(
                    "/topic/notify.user." + alert.getUserId(),
                    msg
            );

            System.out.println("[Rabbit] Alert forwarded to WS for userId=" + alert.getUserId());
        } catch (Exception e) {
            System.err.println("[Rabbit] Failed to parse alert JSON. Body was: " + body);
            e.printStackTrace();
        }
    }
}
