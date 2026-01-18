package com.example.demo.realtimesupportservice.listener;

import com.example.demo.realtimesupportservice.dto.ChatMessage;
import com.example.demo.realtimesupportservice.dto.OverconsumptionAlert;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class OverconsumptionListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public OverconsumptionListener(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "overconsumption.alerts")
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
        } catch (Exception e) {
            // ca să nu moară listener-ul și să vezi clar ce ai primit
            System.err.println("Failed to parse alert JSON: " + body);
            e.printStackTrace();
        }
    }

}
