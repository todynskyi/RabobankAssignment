package nl.rabobank.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class AccountUtils {

    public static String generateAccountNumber() {
        return UUID.randomUUID().toString();
    }
}
