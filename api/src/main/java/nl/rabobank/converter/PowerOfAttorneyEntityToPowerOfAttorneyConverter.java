package nl.rabobank.converter;

import lombok.RequiredArgsConstructor;
import nl.rabobank.authorizations.PowerOfAttorney;
import nl.rabobank.mongo.model.PowerOfAttorneyEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PowerOfAttorneyEntityToPowerOfAttorneyConverter implements Converter<PowerOfAttorneyEntity, PowerOfAttorney> {

    @Override
    public PowerOfAttorney convert(final PowerOfAttorneyEntity powerOfAttorneyEntity) {
        return PowerOfAttorney.builder()
                .account(powerOfAttorneyEntity.getAccount())
                .grantorName(powerOfAttorneyEntity.getGrantor())
                .granteeName(powerOfAttorneyEntity.getGrantee())
                .authorization(powerOfAttorneyEntity.getAuthorization())
                .build();
    }
}
