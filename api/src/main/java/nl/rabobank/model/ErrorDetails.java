package nl.rabobank.model;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class ErrorDetails {
    String message;
}
