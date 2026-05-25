package com.exam.web.dto.auth;

import com.exam.model.enums.UserRole;

public record AuthResponseDto(
        String accessToken,
        String username,
        String email,
        UserRole role
) {}
