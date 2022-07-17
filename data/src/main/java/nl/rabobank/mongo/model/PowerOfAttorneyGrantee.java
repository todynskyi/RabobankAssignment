package nl.rabobank.mongo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import nl.rabobank.authorizations.Authorization;

@Data
@AllArgsConstructor
public class PowerOfAttorneyGrantee {
    private final String granteeName;
    private final Authorization authorization;
}
