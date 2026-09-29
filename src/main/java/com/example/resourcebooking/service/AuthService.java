package com.example.resourcebooking.service;

import com.example.resourcebooking.dto.request.LoginRequest;
import com.example.resourcebooking.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
