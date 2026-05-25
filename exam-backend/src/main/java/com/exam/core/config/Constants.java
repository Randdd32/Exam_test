package com.exam.core.config;

public final class Constants {
    public static final String API_URL = "/api/v1";

    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*_=+\\\\-]).{8,60}$";

    private Constants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
