package nl.rabobank.converter;

import lombok.RequiredArgsConstructor;
import nl.rabobank.account.Account;
import nl.rabobank.authorizations.PowerOfAttorney;
import nl.rabobank.mongo.model.AccountEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
public class AccountsToPowerOfAttorneyConverter implements Converter<Pair<List<AccountEntity>, String>, List<PowerOfAttorney>> {

    private final Converter<AccountEntity, Account> toAccountConverter;

    @Override
    public List<PowerOfAttorney> convert(final Pair<List<AccountEntity>, String> pair) {
        return pair.getFirst()
                .stream()
                .flatMap(account -> toPowerOfAttorneys(account, pair.getSecond()))
                .collect(Collectors.toList());
    }

    private Stream<PowerOfAttorney> toPowerOfAttorneys(AccountEntity account, String granteeName) {
        return account.getGrantees()
                .stream()
                .filter(grantee -> grantee.getGranteeName().equals(granteeName))
                .map(grantee -> PowerOfAttorney.builder()
                        .grantorName(account.getAccountHolderName())
                        .granteeName(grantee.getGranteeName())
                        .account(toAccountConverter.convert(account))
                        .authorization(grantee.getAuthorization())
                        .build());
    }
}
