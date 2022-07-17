package nl.rabobank.service;

import nl.rabobank.account.Account;
import nl.rabobank.account.AccountType;
import nl.rabobank.authorizations.Authorization;
import nl.rabobank.authorizations.PowerOfAttorney;
import nl.rabobank.exception.AccountNotFoundException;
import nl.rabobank.exception.PowerOfAttorneySecurityException;
import nl.rabobank.model.CreatePowerOfAttorneyDto;
import nl.rabobank.mongo.model.AccountEntity;
import nl.rabobank.mongo.model.PowerOfAttorneyGrantee;
import nl.rabobank.mongo.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.util.Pair;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.rabobank.mongo.model.Profiles.MONGO_DATA_PROFILE;
import static nl.rabobank.util.AccountTestDataUtils.createAccountEntity;
import static nl.rabobank.util.AccountUtils.generateAccountNumber;
import static nl.rabobank.util.PowerOfAttorneyTestDataUtils.createPowerOfAttorneyDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles(profiles = {MONGO_DATA_PROFILE})
public class PowerOfAttorneyServiceITest {

    @Autowired
    private PowerOfAttorneyService powerOfAttorneyService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private Converter<AccountEntity, Account> toAccountConverter;
    @Autowired
    private Converter<Pair<List<AccountEntity>, String>, List<PowerOfAttorney>> toPowerOfAttorneys;

    @AfterEach
    public void after() {
        accountRepository.deleteAll();
    }

    @DisplayName("Should create Power Of Attorney for Saving and Payment accounts")
    @ParameterizedTest(name = "{index} Authorization: {0} granted to Account Type: {1}")
    @MethodSource("nl.rabobank.util.PowerOfAttorneyTestDataUtils#source")
    public void shouldGrantAccess(Authorization authorization, AccountType accountType) {
        CreatePowerOfAttorneyDto powerOfAttorneyDto = createPowerOfAttorneyDto(authorization);
        AccountEntity account = accountRepository.save(createAccountEntity(powerOfAttorneyDto.getAccountNumber(), powerOfAttorneyDto.getGrantor(), accountType, 0.));

        PowerOfAttorney powerOfAttorney = powerOfAttorneyService.grantAccess(powerOfAttorneyDto);

        assertEquals(powerOfAttorneyDto.getGrantor(), powerOfAttorney.getGrantorName());
        assertEquals(powerOfAttorneyDto.getGrantee(), powerOfAttorney.getGranteeName());
        assertEquals(authorization, powerOfAttorney.getAuthorization());

        Account grantedAccount = powerOfAttorney.getAccount();
        assertEquals(powerOfAttorneyDto.getGrantor(), grantedAccount.getAccountHolderName());
        assertEquals(powerOfAttorneyDto.getAccountNumber(), grantedAccount.getAccountNumber());
        assertEquals(account.getBalance(), grantedAccount.getBalance());

        List<AccountEntity> accounts = accountRepository.findAccountEntityByGrantees_GranteeNameOrderById(powerOfAttorneyDto.getGrantee());
        assertEquals(1, accounts.size());

        AccountEntity granteeAccount = accounts.get(0);
        assertNotNull(granteeAccount);

        assertEquals(powerOfAttorneyDto.getGrantor(), granteeAccount.getAccountHolderName());
        assertEquals(powerOfAttorneyDto.getAccountNumber(), granteeAccount.getAccountNumber());
        assertEquals(account.getBalance(), granteeAccount.getBalance());

        assertTrue(granteeAccount.getGrantees().contains(new PowerOfAttorneyGrantee(powerOfAttorneyDto.getGrantee(), authorization)));
    }

    @DisplayName("Should return PowerOfAttorneySecurityException in case of same Grantor and Grantee")
    @ParameterizedTest(name = "{index} Authorization: {0}")
    @EnumSource(value = Authorization.class)
    public void shouldReturnSecurityExceptionInCaseOfSameGrantorAndGrantee(Authorization authorization) {
        CreatePowerOfAttorneyDto powerOfAttorneyDto = createPowerOfAttorneyDto(authorization);
        powerOfAttorneyDto.setGrantee(powerOfAttorneyDto.getGrantor());

        PowerOfAttorneySecurityException thrown = Assertions.assertThrows(PowerOfAttorneySecurityException.class, () ->
                powerOfAttorneyService.grantAccess(powerOfAttorneyDto)
        );
        assertEquals("Grantor cannot give access to himself/herself", thrown.getMessage());

    }

    @DisplayName("Should return PowerOfAttorneySecurityException in case of granting to not own account")
    @ParameterizedTest(name = "{index} Authorization: {0} granted to Account Type: {1}")
    @MethodSource("nl.rabobank.util.PowerOfAttorneyTestDataUtils#source")
    public void shouldReturnSecurityExceptionInCaseOfGrantingToNotOwnAccount(Authorization authorization, AccountType accountType) {
        CreatePowerOfAttorneyDto powerOfAttorneyDto = createPowerOfAttorneyDto(authorization);
        accountRepository.save(createAccountEntity(powerOfAttorneyDto.getAccountNumber(), "other Holder", accountType, 0.));

        PowerOfAttorneySecurityException thrown = Assertions.assertThrows(PowerOfAttorneySecurityException.class, () ->
                powerOfAttorneyService.grantAccess(powerOfAttorneyDto)
        );
        assertEquals("Grantor can give access only for own account", thrown.getMessage());
    }

    @DisplayName("Should return AccountNotFoundException in case of missing account")
    @ParameterizedTest(name = "{index} Authorization: {0}")
    @EnumSource(value = Authorization.class)
    public void shouldReturnAccountNotFoundExceptionInCaseOfMissingAccount(Authorization authorization) {
        CreatePowerOfAttorneyDto powerOfAttorneyDto = createPowerOfAttorneyDto(authorization);
        powerOfAttorneyDto.setAccountNumber("fake number");

        AccountNotFoundException thrown = Assertions.assertThrows(AccountNotFoundException.class, () ->
                powerOfAttorneyService.grantAccess(powerOfAttorneyDto)
        );
        assertEquals("Cannot find account by account number: fake number", thrown.getMessage());
    }

    @DisplayName("Should fetch Granted Accounts")
    @Test
    public void shouldFetchGrantedAccounts() {
        String grantee = "grantee";

        AccountEntity ownAccount = accountRepository.save(createAccountEntity(generateAccountNumber(), grantee, AccountType.SAVINGS, 100.));
        AccountEntity account1 = accountRepository.save(createAccountEntity(generateAccountNumber(), "Holder1", AccountType.SAVINGS, 100.));
        AccountEntity account2 = accountRepository.save(createAccountEntity(generateAccountNumber(), "Holder2", AccountType.PAYMENT, 100.));
        accountRepository.save(createAccountEntity(generateAccountNumber(), "Holder3", AccountType.SAVINGS, 100.));

        accountRepository.save(account1.addGrantee(new PowerOfAttorneyGrantee(ownAccount.getAccountHolderName(), Authorization.READ)));
        accountRepository.save(account1.addGrantee(new PowerOfAttorneyGrantee(account2.getAccountHolderName(), Authorization.READ)));
        accountRepository.save(account2.addGrantee(new PowerOfAttorneyGrantee(ownAccount.getAccountHolderName(), Authorization.READ)));
        accountRepository.save(account2.addGrantee(new PowerOfAttorneyGrantee(ownAccount.getAccountHolderName(), Authorization.WRITE)));

        List<Account> expected = Stream.of(account1, account2).map(toAccountConverter::convert).collect(Collectors.toList());

        List<Account> grantedAccounts = powerOfAttorneyService.getGrantedAccounts(grantee);
        assertEquals(2, grantedAccounts.size());
        assertEquals(expected, grantedAccounts);
    }

    @DisplayName("Should fetch Power Of Attorneys")
    @Test
    public void shouldFetchPowerOfAttorneys() {
        String grantee = "grantee";

        AccountEntity ownAccount = accountRepository.save(createAccountEntity(generateAccountNumber(), grantee, AccountType.SAVINGS, 100.));
        AccountEntity account1 = accountRepository.save(createAccountEntity(generateAccountNumber(), "Holder1", AccountType.SAVINGS, 100.));
        AccountEntity account2 = accountRepository.save(createAccountEntity(generateAccountNumber(), "Holder2", AccountType.PAYMENT, 100.));
        accountRepository.save(createAccountEntity(generateAccountNumber(), "Holder3", AccountType.SAVINGS, 100.));

        account1 = accountRepository.save(account1.addGrantee(new PowerOfAttorneyGrantee(ownAccount.getAccountHolderName(), Authorization.READ)));
        account1 = accountRepository.save(account1.addGrantee(new PowerOfAttorneyGrantee(account2.getAccountHolderName(), Authorization.READ)));
        account2 = accountRepository.save(account2.addGrantee(new PowerOfAttorneyGrantee(ownAccount.getAccountHolderName(), Authorization.READ)));
        account2 = accountRepository.save(account2.addGrantee(new PowerOfAttorneyGrantee(ownAccount.getAccountHolderName(), Authorization.WRITE)));


        List<PowerOfAttorney> expected = toPowerOfAttorneys.convert(Pair.of(List.of(account1, account2), grantee));

        List<PowerOfAttorney> powerOfAttorneys = powerOfAttorneyService.getPowerOfAttorneys(grantee);
        assertEquals(3, powerOfAttorneys.size());
        assertEquals(expected, powerOfAttorneys);
    }
}
