package com.exam.web.dto.filter;

import com.exam.model.enums.UserRole;

import java.time.Instant;
import java.util.List;

public record UserFilter(
        String search,
        List<UserRole> roles,
        Instant createdAfter,
        Instant createdBefore,
        Instant updatedAfter,
        Instant updatedBefore
) {}
