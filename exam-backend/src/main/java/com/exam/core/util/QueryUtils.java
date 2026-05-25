package com.exam.core.util;

public final class QueryUtils {
    public static String cleanSearchToken(String search) {
        return (search != null && !search.isBlank()) ? search.toLowerCase().trim() : null;
    }

    private QueryUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
