package com.exam.web.dto.filter;

import java.time.Instant;

public record CategoryFilter(String search, Instant createdAfter, Instant createdBefore, Instant updatedAfter, Instant updatedBefore) {}
