package com.ritesh.crypto_portfolio_tracker.service;

import com.ritesh.crypto_portfolio_tracker.entity.RiskAlert;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseNotificationService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Register a user for SSE streaming.
     * Called by GET /api/sse/alerts/{userId}
     */
    public SseEmitter createEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        return emitter;
    }

    /**
     * Push risk alert via SSE to a connected user.
     */
    public void sendToUser(Long userId, RiskAlert alert) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;

        try {
            emitter.send(alert);
        } catch (IOException e) {
            emitters.remove(userId);
        }
    }
}
