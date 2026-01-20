package com.tricol.services;

import com.tricol.dtos.request.LoginRequest;
import com.tricol.dtos.request.RegisterRequest;
import com.tricol.dtos.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
