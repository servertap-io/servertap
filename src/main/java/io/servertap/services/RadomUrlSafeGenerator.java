package io.servertap.services;

import java.security.SecureRandom;

public class RadomUrlSafeGenerator {
    private static final String URL_SAFE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

    public static String generateRandomURLSafeString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0.");
        }

        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[length];

        random.nextBytes(randomBytes);

        StringBuilder urlSafeString = new StringBuilder();

        for (byte b : randomBytes) {
            // Ensure the generated character is within the URL-safe character set
            int index = Math.abs(b) % URL_SAFE_CHARACTERS.length();
            urlSafeString.append(URL_SAFE_CHARACTERS.charAt(index));
        }

        return urlSafeString.toString();
    }
}