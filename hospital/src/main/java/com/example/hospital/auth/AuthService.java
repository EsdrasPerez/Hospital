package com.example.hospital.auth;

import com.example.hospital.auth.dto.LoginRequest;
import com.example.hospital.auth.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    void register(LoginRequest request);
}
