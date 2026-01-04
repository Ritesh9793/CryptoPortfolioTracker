package com.ritesh.crypto_portfolio_tracker.service;

import com.ritesh.crypto_portfolio_tracker.entity.User;
import com.ritesh.crypto_portfolio_tracker.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    public String getEmailByUserId(Long id) {
        return users.findById(id)
                .map(User::getEmail)
                .orElse(null);
    }
}
