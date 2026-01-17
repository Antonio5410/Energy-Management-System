package com.example.demo.messaging;

import com.example.demo.dtos.SyncEventDTO;
import com.example.demo.services.AuthService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SyncEventListener {

    private final AuthService authService;

    public SyncEventListener(AuthService authService) {
        this.authService = authService;
    }

    @RabbitListener(queues = "${sync.queue}")
    public void handleSyncEvent(SyncEventDTO event) {
        if (event == null || event.getEventType() == null) return;

        switch (event.getEventType()) {
            case "USER_UPDATED" -> authService.syncUserUpdated(
                    event.getUserId(),
                    event.getUsername(),
                    event.getRole()
            );
            case "USER_DELETED" -> authService.syncUserDeleted(event.getUserId());

            // recomand să ignori USER_CREATED în auth, ca să nu creezi cred fără parolă
            case "USER_CREATED" -> {
                // optional: log only
            }

            default -> {
                // ignore
            }
        }
    }
}
