package com.adarsh.airesumeanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object representing the authentication response payload.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    /**
     * The JSON Web Token (JWT) issued upon successful authentication.
     */
    private String token;

    /**
     * Descriptive outcome message for the authentication request.
     */
    private String message;
}
