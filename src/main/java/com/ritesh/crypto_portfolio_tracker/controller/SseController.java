package com.ritesh.crypto_portfolio_tracker.controller;

import com.ritesh.crypto_portfolio_tracker.service.SseNotificationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
public class SseController {

    private final SseNotificationService sseService;

    public SseController(SseNotificationService sseService) {
        this.sseService = sseService;
    }

    @GetMapping("/alerts/{userId}")
    public SseEmitter alerts(@PathVariable Long userId) {
        return sseService.createEmitter(userId);
    }
}
