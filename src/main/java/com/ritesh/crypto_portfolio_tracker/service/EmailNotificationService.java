package com.ritesh.crypto_portfolio_tracker.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final UserService userService;

    public EmailNotificationService(JavaMailSender mailSender,
                                    UserService userService) {
        this.mailSender = mailSender;
        this.userService = userService;
    }

    public void sendRiskEmail(Long userId, String subject, String message) {
        String email = userService.getEmailByUserId(userId);
        if (email == null) return;

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(email);
        mail.setSubject(subject);
        mail.setText(message);

        mailSender.send(mail);
    }
}
