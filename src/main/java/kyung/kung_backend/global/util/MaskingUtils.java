package kyung.kung_backend.global.util;

import java.util.regex.Pattern;

public final class MaskingUtils {

    private static final Pattern RESIDENT_REGISTRATION_NUMBER_PATTERN =
            Pattern.compile("^\\d{6}-\\d{7}$");

    private MaskingUtils() {
    }

    public static String maskResidentRegistrationNumber(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();

        if (!RESIDENT_REGISTRATION_NUMBER_PATTERN.matcher(trimmed).matches()) {
            return null;
        }

        return trimmed.substring(0, 8) + "******";
    }
}
