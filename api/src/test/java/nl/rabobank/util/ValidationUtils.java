package nl.rabobank.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationUtils {

    public static final String NOT_BLANK_VALIDATION_MESSAGE = "must not be blank";
    public static final String NOT_NULL_VALIDATION_MESSAGE = "must not be null";
    public static final String POSITIVE_OR_ZERO_VALIDATION_MESSAGE = "must be greater than or equal to 0";
}
