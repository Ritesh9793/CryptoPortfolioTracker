package com.ritesh.crypto_portfolio_tracker.service;

import com.ritesh.crypto_portfolio_tracker.entity.RiskAlert;
import com.ritesh.crypto_portfolio_tracker.repository.RiskAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final RiskAlertRepository alertRepo;
    private final RealtimeNotificationService realtimeNotificationService; // WebSocket
    private final SseNotificationService sseNotificationService;            // SSE
    private final EmailNotificationService emailNotificationService;        // Email

    public NotificationService(RiskAlertRepository alertRepo,
                               RealtimeNotificationService realtimeNotificationService,
                               SseNotificationService sseNotificationService,
                               EmailNotificationService emailNotificationService) {

        this.alertRepo = alertRepo;
        this.realtimeNotificationService = realtimeNotificationService;
        this.sseNotificationService = sseNotificationService;
        this.emailNotificationService = emailNotificationService;
    }

    @Transactional
    public RiskAlert createAlert(Long userId,
                                 String assetSymbol,
                                 String type,
                                 String details,
                                 boolean emailNotify) {

        RiskAlert alert = RiskAlert.builder()
                .userId(userId)
                .assetSymbol(assetSymbol)
                .alertType(type)
                .details(details)
                .isRead(false)
                .build();

        RiskAlert saved = alertRepo.save(alert);

        // ---- Realtime push notifications ----
        realtimeNotificationService.pushAlertToUser(userId, saved);

        // ---- SSE push ----
        sseNotificationService.sendToUser(userId, saved);

        // ---- Email notification (optional) ----
        if (emailNotify) {
            emailNotificationService.sendRiskEmail(
                    userId,
                    "Risk Alert: " + assetSymbol,
                    details
            );
        }

        return saved;
    }
}
