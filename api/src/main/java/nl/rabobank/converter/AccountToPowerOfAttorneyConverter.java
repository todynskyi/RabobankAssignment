package nl.rabobank.converter;

import lombok.RequiredArgsConstructor;
import nl.rabobank.account.Account;
import nl.rabobank.authorizations.PowerOfAttorney;
import nl.rabobank.model.CreatePowerOfAttorneyDto;
import nl.rabobank.mongo.model.AccountEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AccountToPowerOfAttorneyConverter implements Converter<Pair<CreatePowerOfAttorneyDto, AccountEntity>, PowerOfAttorney> {

    private final Converter<AccountEntity, Account> toAccountConverter;

    @Override
    public PowerOfAttorney convert(final Pair<CreatePowerOfAttorneyDto, AccountEntity> pair) {
        CreatePowerOfAttorneyDto powerOfAttorneyDto = pair.getFirst();
        Account account = toAccountConverter.convert(pair.getSecond());
        return PowerOfAttorney.builder()
                .account(account)
                .authorization(powerOfAttorneyDto.getAuthorization())
                .grantorName(powerOfAttorneyDto.getGrantor())
                .granteeName(powerOfAttorneyDto.getGrantee())
                .build();
    }
}
