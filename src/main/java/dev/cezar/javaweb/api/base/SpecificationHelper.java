package dev.cezar.javaweb.api.base;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpecificationHelper {

    public static String ilike(String value) {
        return "%" + value + "%";
    }

    public static boolean isValidUUID(String uuidAsString) {
        try {
            UUID.fromString(uuidAsString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
