package com.adarsh.airesumeanalyzer.service;

import com.adarsh.airesumeanalyzer.dto.AuthResponse;
import com.adarsh.airesumeanalyzer.dto.LoginRequest;
import com.adarsh.airesumeanalyzer.dto.RegisterRequest;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    /**
     * Registers a new user in the system.
     *
     * @param request registration request containing user details
     * @return AuthResponse containing authentication response data
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates an existing user in the system.
     *
     * @param request login request containing email and raw password
     * @return AuthResponse containing authentication response data
     */
    AuthResponse login(LoginRequest request);
}
