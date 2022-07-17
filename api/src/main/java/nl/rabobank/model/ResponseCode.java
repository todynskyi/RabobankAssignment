package nl.rabobank.model;

import lombok.experimental.FieldNameConstants;
import lombok.experimental.UtilityClass;

@UtilityClass
@FieldNameConstants
public class ResponseCode {

    public static final String OK = "200";
    public static final String CREATED = "201";
    public static final String BAD_REQUEST = "400";
    public static final String UNAUTHORIZED = "401";
    public static final String NOT_FOUND = "404";
}
