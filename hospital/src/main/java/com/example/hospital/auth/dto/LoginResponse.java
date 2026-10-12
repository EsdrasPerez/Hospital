package com.example.hospital.auth.dto;

public record LoginResponse(
    String token,
    String type
) {}
