package com.example.loggingym.api.dto;

public class LoginRequest {

    public final String dni;
    public final String password;

    public LoginRequest(String dni, String password) {
        this.dni = dni;
        this.password = password;
    }
}
