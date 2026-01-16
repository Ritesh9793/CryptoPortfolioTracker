package com.ritesh.crypto_portfolio_tracker.controller;


import com.ritesh.crypto_portfolio_tracker.dto.auth.LoginRequest;
import com.ritesh.crypto_portfolio_tracker.dto.auth.RegisterRequest;
import com.ritesh.crypto_portfolio_tracker.entity.User;
import com.ritesh.crypto_portfolio_tracker.repository.UserRepository;
import com.ritesh.crypto_portfolio_tracker.config.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest){
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent())
            return ResponseEntity.badRequest().body("Email already exists");

        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid Password");

        String token = jwtService.generateToken(loginRequest.getEmail());

        return ResponseEntity.ok(Map.of("token", token, "userId", user.getId()));
    }


}
