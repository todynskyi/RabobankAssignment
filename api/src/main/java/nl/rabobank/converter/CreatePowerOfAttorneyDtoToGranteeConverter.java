package nl.rabobank.converter;

import nl.rabobank.model.CreatePowerOfAttorneyDto;
import nl.rabobank.mongo.model.PowerOfAttorneyGrantee;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CreatePowerOfAttorneyDtoToGranteeConverter implements Converter<CreatePowerOfAttorneyDto, PowerOfAttorneyGrantee> {

    @Override
    public PowerOfAttorneyGrantee convert(CreatePowerOfAttorneyDto createPowerOfAttorneyDto) {
        return new PowerOfAttorneyGrantee(createPowerOfAttorneyDto.getGrantee(), createPowerOfAttorneyDto.getAuthorization());
    }
}
