package nl.rabobank.util;

import lombok.experimental.UtilityClass;
import nl.rabobank.account.AccountType;
import nl.rabobank.authorizations.Authorization;
import nl.rabobank.authorizations.PowerOfAttorney;
import nl.rabobank.model.CreatePowerOfAttorneyDto;
import org.junit.jupiter.params.provider.Arguments;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static nl.rabobank.util.AccountTestDataUtils.createAccount;
import static nl.rabobank.util.AccountUtils.generateAccountNumber;

@UtilityClass
public class PowerOfAttorneyTestDataUtils {

    public static CreatePowerOfAttorneyDto createPowerOfAttorneyDto(Authorization authorization) {
        CreatePowerOfAttorneyDto powerOfAttorneyDto = new CreatePowerOfAttorneyDto();
        powerOfAttorneyDto.setGrantor("Grantor " + Instant.now().getEpochSecond());
        powerOfAttorneyDto.setGrantee("Grantee " + Instant.now().getEpochSecond());
        powerOfAttorneyDto.setAuthorization(authorization);
        powerOfAttorneyDto.setAccountNumber(generateAccountNumber());
        return powerOfAttorneyDto;
    }

    public static PowerOfAttorney toPowerOfAttorney(CreatePowerOfAttorneyDto powerOfAttorneyDto, AccountType accountType) {
        return PowerOfAttorney.builder()
                .grantorName(powerOfAttorneyDto.getGrantor())
                .granteeName(powerOfAttorneyDto.getGrantee())
                .authorization(powerOfAttorneyDto.getAuthorization())
                .account(createAccount(powerOfAttorneyDto.getAccountNumber(), powerOfAttorneyDto.getGrantor(), 0., accountType))
                .build();
    }

    public static PowerOfAttorney createPowerOfAttorney(String grantee, Authorization authorization, AccountType accountType) {
        String grantorName = "Grantor " + Instant.now().getEpochSecond();
        return PowerOfAttorney.builder()
                .grantorName(grantorName)
                .granteeName(grantee)
                .authorization(authorization)
                .account(createAccount(generateAccountNumber(), grantorName, 0., accountType))
                .build();
    }

    public static List<PowerOfAttorney> createPowerOfAttorneys(String grantee) {
        return stream(Authorization.values())
                .flatMap(authorization ->
                        stream(AccountType.values())
                                .map(accountType ->
                                        createPowerOfAttorney(grantee, authorization, accountType)
                                )
                )
                .collect(Collectors.toList());
    }

    public static Stream<Arguments> source() {
        return stream(Authorization.values())
                .flatMap(authorization ->
                        stream(AccountType.values())
                                .map(accountType ->
                                        Arguments.of(authorization, accountType)
                                )
                );
    }
}
