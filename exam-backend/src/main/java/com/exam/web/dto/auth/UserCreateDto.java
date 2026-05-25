package com.exam.web.dto.auth;


import com.exam.core.config.Constants;
import com.exam.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateDto(
        @NotBlank
        @Size(min = 2, max = 100)
        String username,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Pattern(
                regexp = Constants.PASSWORD_PATTERN,
                message = "Password must contain 8-60 characters, at least one uppercase letter, one lowercase letter, one digit, and one special character (!@#$%^&*_=+-)."
        )
        String password,

        @NotNull
        UserRole role
) {}
