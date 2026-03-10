package com.fdymain.fid.utils;

public class WhatsAppConfig {

    public static String getAccessToken() {
        return requireEnv("WA_ACCESS_TOKEN");
    }

    public static String getPhoneNumberId() {
        return requireEnv("WA_PHONE_NUMBER_ID");
    }

    public static String getToNumber() {
        return requireEnv("WA_TO_NUMBER");
    }

    public static String getApiUrl() {
        String val = System.getenv("WA_API_URL");
        return (val != null && !val.isBlank()) ? val : "https://graph.facebook.com/v22.0/";
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
