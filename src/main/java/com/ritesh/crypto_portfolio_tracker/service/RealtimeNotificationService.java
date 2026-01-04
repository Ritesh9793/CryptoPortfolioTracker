package com.ritesh.crypto_portfolio_tracker.service;

import com.ritesh.crypto_portfolio_tracker.entity.RiskAlert;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RealtimeNotificationService {

    private final SimpMessagingTemplate messaging;

    public RealtimeNotificationService(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    /**
     * Push alert via WebSocket STOMP to: /user/{userId}/queue/alerts
     */
    public void pushAlertToUser(Long userId, RiskAlert alert) {
        messaging.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/alerts",
                alert
        );
    }
}
