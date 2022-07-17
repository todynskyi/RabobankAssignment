package nl.rabobank.util;

import nl.rabobank.account.Account;
import nl.rabobank.account.AccountType;
import nl.rabobank.account.PaymentAccount;
import nl.rabobank.account.SavingsAccount;
import nl.rabobank.model.CreateAccountDto;
import nl.rabobank.mongo.model.AccountEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static nl.rabobank.util.AccountUtils.generateAccountNumber;

public final class AccountTestDataUtils {

    public static CreateAccountDto createAccountDto(AccountType type) {
        CreateAccountDto accountDto = new CreateAccountDto();
        accountDto.setAccountHolderName(generateName("Name"));
        accountDto.setInitialBalance(100.0);
        accountDto.setType(type);
        return accountDto;
    }

    public static Account toAccount(CreateAccountDto accountDto) {
        return createAccount(generateAccountNumber(), accountDto.getAccountHolderName(), accountDto.getInitialBalance(), accountDto.getType());
    }

    public static List<Account> createAccounts() {
        List<Account> accounts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            accounts.add(createPaymentAccount(generateAccountNumber(), generateName("Payment ", i), 100.0 + i));
            accounts.add(createSavingsAccount(generateAccountNumber(), generateName("Savings ", i), 100.0 + i));
        }
        return accounts;
    }

    public static List<Account> createAccounts(String accountHolderName) {
        List<Account> accounts = new ArrayList<>();
        accounts.add(createPaymentAccount(generateAccountNumber(), accountHolderName, 100.0));
        accounts.add(createSavingsAccount(generateAccountNumber(), accountHolderName, 1000.0));
        return accounts;
    }

    public static Account createAccount(String accountNumber, String accountHolderName, Double balance, AccountType type) {
        if (type == AccountType.SAVINGS) {
            return createSavingsAccount(accountNumber, accountHolderName, balance);
        }
        return createPaymentAccount(accountNumber, accountHolderName, balance);
    }

    public static AccountEntity createAccountEntity(String accountNumber, AccountType accountType) {
        return createAccountEntity(accountNumber, accountType, 100.);
    }

    public static AccountEntity createAccountEntity(String accountNumber, AccountType accountType, Double balance) {
        return createAccountEntity(accountNumber, generateName("Holder name"), accountType, balance);
    }

    public static AccountEntity createAccountEntity(String accountNumber, String accountHolderName, AccountType accountType, Double balance) {
        return AccountEntity.builder()
                .accountNumber(accountNumber)
                .accountHolderName(accountHolderName)
                .balance(balance)
                .type(accountType)
                .build();
    }

    public static List<AccountEntity> createAccountEntities() {
        return Arrays.asList(
                createAccountEntity(generateAccountNumber(), AccountType.PAYMENT, 200.),
                createAccountEntity(generateAccountNumber(), AccountType.SAVINGS, 250.),
                createAccountEntity(generateAccountNumber(), AccountType.PAYMENT, 10.),
                createAccountEntity(generateAccountNumber(), AccountType.SAVINGS, 230.)
        );
    }

    private static Account createPaymentAccount(String accountNumber, String accountHolderName, Double balance) {
        return new PaymentAccount(accountNumber, accountHolderName, balance);
    }

    private static Account createSavingsAccount(String accountNumber, String accountHolderName, Double balance) {
        return new SavingsAccount(accountNumber, accountHolderName, balance);
    }

    private static String generateName(String prefix) {
        return generateName(prefix, Instant.now().getEpochSecond());
    }

    private static String generateName(String prefix, long unique) {
        return prefix + "_" + unique;
    }
}
