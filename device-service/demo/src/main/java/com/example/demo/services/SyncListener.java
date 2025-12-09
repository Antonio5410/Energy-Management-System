package com.example.demo.services;

import com.example.demo.dtos.SyncEventDTO;
import com.example.demo.entities.SyncedUser;
import com.example.demo.repositories.SyncedUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class SyncListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncListener.class);

    private final SyncedUserRepository syncedUserRepository;

    public SyncListener(SyncedUserRepository syncedUserRepository) {
        this.syncedUserRepository = syncedUserRepository;
    }

    @RabbitListener(queues = "sync.queue")
    public void handleSyncEvent(SyncEventDTO event) {
        if (!"USER_CREATED".equals(event.getEventType())) {
            LOGGER.info("Ignoring event of type {}", event.getEventType());
            return;
        }

        SyncedUser user = new SyncedUser();
        user.setId(event.getUserId());
        user.setUsername(event.getUsername());

        syncedUserRepository.save(user);
        LOGGER.info("Synced user {} ({}) into device-service DB",
                event.getUserId(), event.getUsername());
    }
}
