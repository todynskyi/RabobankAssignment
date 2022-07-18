package nl.rabobank.converter;

import nl.rabobank.model.CreatePowerOfAttorneyDto;
import nl.rabobank.mongo.model.AccountEntity;
import nl.rabobank.mongo.model.PowerOfAttorneyEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Component
public class CreatePowerOfAttorneyDtoToPowerOfAttorneyEntityConverter implements Converter<Pair<CreatePowerOfAttorneyDto, AccountEntity>, PowerOfAttorneyEntity> {

    @Override
    public PowerOfAttorneyEntity convert(final Pair<CreatePowerOfAttorneyDto, AccountEntity> pair) {
        CreatePowerOfAttorneyDto createPowerOfAttorneyDto = pair.getFirst();
        return PowerOfAttorneyEntity.builder()
                .grantor(createPowerOfAttorneyDto.getGrantor())
                .grantee(createPowerOfAttorneyDto.getGrantee())
                .authorization(createPowerOfAttorneyDto.getAuthorization())
                .account(pair.getSecond())
                .build();
    }
}
