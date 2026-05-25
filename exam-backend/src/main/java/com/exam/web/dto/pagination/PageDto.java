package com.exam.web.dto.pagination;

import java.util.List;

public record PageDto<T>(
        List<T> items,
        int itemsCount,
        int currentPage,
        int currentSize,
        int totalPages,
        long totalItems,
        boolean isFirst,
        boolean isLast,
        boolean hasNext,
        boolean hasPrevious
) {}
