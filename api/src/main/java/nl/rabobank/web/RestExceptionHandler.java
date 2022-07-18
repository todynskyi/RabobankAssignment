package nl.rabobank.web;

import com.mongodb.MongoWriteException;
import nl.rabobank.exception.AccountNotFoundException;
import nl.rabobank.exception.PowerOfAttorneySecurityException;
import nl.rabobank.model.ErrorDetails;
import nl.rabobank.mongo.util.MongoUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(AccountNotFoundException.class)
    public ErrorDetails handleException(AccountNotFoundException ex) {
        return new ErrorDetails(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(PowerOfAttorneySecurityException.class)
    public ErrorDetails handleException(PowerOfAttorneySecurityException ex) {
        return new ErrorDetails(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MongoWriteException.class)
    public ErrorDetails handleException(MongoWriteException ex) {
        if (ex.getMessage().contains(MongoUtils.POWER_OF_ATTORNEY_UNIQUE_INDEX_NAME)) {
            return new ErrorDetails("Authorization already granted");
        }
        return new ErrorDetails(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
