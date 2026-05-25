package com.exam.web.dto.auth;

import com.exam.model.enums.UserRole;

import java.time.Instant;

public record UserDto(
        Long id,
        String username,
        String email,
        UserRole role,
        Instant createdAt,
        Instant updatedAt
) {}
