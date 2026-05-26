package com.exam.web.dto.filter;

import java.time.Instant;
import java.util.List;

public record ItemFilter(
        String search,
        Long authorId,
        Long categoryId,
        List<Long> tagIds,
        Double minValue,
        Double maxValue,
        Instant createdAfter,
        Instant createdBefore,
        Instant updatedAfter,
        Instant updatedBefore
) {}
