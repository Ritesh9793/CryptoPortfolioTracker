package com.ritesh.crypto_portfolio_tracker.dto.auth;


import lombok.Data;

@Data
public class RegisterRequest {

    public String Name;
    public String Email;
    public String Password;
}
