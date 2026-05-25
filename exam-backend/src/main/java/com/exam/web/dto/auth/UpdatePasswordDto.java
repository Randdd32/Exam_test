package com.exam.web.dto.auth;

import com.exam.core.config.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePasswordDto (
        @NotBlank
        @Pattern(
                regexp = Constants.PASSWORD_PATTERN,
                message = "Password must contain 8-60 characters, at least one uppercase letter, one lowercase letter, one digit, and one special character (!@#$%^&*_=+-)."
        )
        String newPassword
) {}
