package com.fdymain.fid.utils;

public class Constants {

    public static String getLoginUrl() {
        return requireEnv("APP_LOGIN_URL");
    }

    public static String getUsername() {
        return requireEnv("APP_USERNAME");
    }

    public static String getPassword() {
        return requireEnv("APP_PASSWORD");
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Variable de entorno requerida no configurada: " + name);
        }
        return value;
    }
}
