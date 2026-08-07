package com.adarsh.airesumeanalyzer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object representing user authentication credentials.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    /**
     * The registered email address of the user attempting authentication.
     */
    @NotBlank(message = "Email address is required")
    @Email(message = "Email address must be a valid email format")
    private String email;

    /**
     * The raw password provided for authentication.
     */
    @NotBlank(message = "Password is required")
    private String password;
}
