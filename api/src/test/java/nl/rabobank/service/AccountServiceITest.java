package nl.rabobank.service;


import nl.rabobank.account.Account;
import nl.rabobank.account.AccountType;
import nl.rabobank.exception.AccountNotFoundException;
import nl.rabobank.model.CreateAccountDto;
import nl.rabobank.mongo.model.AccountEntity;
import nl.rabobank.mongo.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static nl.rabobank.mongo.model.Profiles.MONGO_DATA_PROFILE;
import static nl.rabobank.util.AccountTestDataUtils.createAccountDto;
import static nl.rabobank.util.AccountTestDataUtils.createAccountEntities;
import static nl.rabobank.util.AccountTestDataUtils.createAccountEntity;
import static nl.rabobank.util.AccountUtils.generateAccountNumber;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles(profiles = {MONGO_DATA_PROFILE})
public class AccountServiceITest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private Converter<AccountEntity, Account> toAccountConverter;

    @AfterEach
    public void after() {
        accountRepository.deleteAll();
    }

    @DisplayName("Should create Payment and Savings accounts")
    @ParameterizedTest(name = "{index} Created Account Type: {0}")
    @EnumSource(value = AccountType.class)
    public void shouldCreateAccount(AccountType accountType) {
        CreateAccountDto accountDto = createAccountDto(accountType);

        Account savedAccount = accountService.createAccount(accountDto);

        assertEquals(accountDto.getAccountHolderName(), savedAccount.getAccountHolderName());
        assertEquals(accountDto.getInitialBalance(), savedAccount.getBalance());
        assertNotNull(savedAccount.getAccountNumber());

        Optional<AccountEntity> fetchedAccountOpt = accountRepository.findAccountEntityByAccountNumber(savedAccount.getAccountNumber());
        assertTrue(fetchedAccountOpt.isPresent());

        AccountEntity fetchedAccount = fetchedAccountOpt.get();

        assertEquals(accountDto.getAccountHolderName(), fetchedAccount.getAccountHolderName());
        assertEquals(accountDto.getInitialBalance(), fetchedAccount.getBalance());
        assertEquals(accountType, fetchedAccount.getType());
    }

    @DisplayName("Should fetch Payment and Savings account by account number")
    @ParameterizedTest(name = "{index} Fetched Account Type: {0}")
    @EnumSource(value = AccountType.class)
    public void shouldFetchAccountByAccountNumber(AccountType accountType) {
        String accountNumber = generateAccountNumber();
        AccountEntity savedAccount = accountRepository.save(createAccountEntity(accountNumber, accountType));

        Account account = accountService.getAccount(accountNumber);

        assertEquals(accountNumber, account.getAccountNumber());
        assertEquals(savedAccount.getAccountHolderName(), account.getAccountHolderName());
        assertEquals(savedAccount.getBalance(), account.getBalance());
    }

    @DisplayName("Should throw AccountNotFoundException for not existed account")
    @Test
    public void shouldThrowExceptionForNotExistedAccountNumber() {
        AccountNotFoundException thrown = Assertions.assertThrows(AccountNotFoundException.class, () ->
                accountService.getAccount("fake account number")
        );
        assertEquals("Cannot find account by account number: fake account number", thrown.getMessage());
    }

    @DisplayName("Should fetch Payment and Savings accounts")
    @Test
    public void shouldFetchAccounts() {
        List<AccountEntity> accounts = accountRepository.saveAll(createAccountEntities());
        List<Account> expected = accounts.stream()
                .sorted(Comparator.comparing(AccountEntity::getBalance))
                .limit(3)
                .map(toAccountConverter::convert)
                .collect(Collectors.toList());


        List<Account> fetchedAccount = accountService.getAccounts(PageRequest.of(0, 3, Sort.by(Sort.Order.asc("balance"))));

        assertEquals(expected, fetchedAccount);
    }
}
