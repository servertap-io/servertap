package io.servertap.api.v1;

import java.util.UUID;

public class ValidationUtils {

    public static UUID safeUUID(String input) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    public static Integer safeInteger(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

}
