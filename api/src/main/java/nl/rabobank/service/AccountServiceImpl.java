package nl.rabobank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.rabobank.account.Account;
import nl.rabobank.exception.AccountNotFoundException;
import nl.rabobank.model.CreateAccountDto;
import nl.rabobank.mongo.model.AccountEntity;
import nl.rabobank.mongo.repository.AccountRepository;
import nl.rabobank.util.AccountUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final Converter<AccountEntity, Account> toAccountConverter;
    private final Converter<CreateAccountDto, AccountEntity> toAccountEntityConverter;

    @Transactional(readOnly = true)
    @Override
    public List<Account> getAccounts(Pageable page) {
        log.debug("Fetching accounts. {}", page);
        return accountRepository.findAll(page)
                .map(toAccountConverter::convert)
                .getContent();
    }

    @Transactional(readOnly = true)
    @Override
    public Account getAccount(String accountNumber) {
        log.debug("Fetching account by accountNumber: {}", accountNumber);
        return accountRepository.findAccountEntityByAccountNumber(accountNumber)
                .map(toAccountConverter::convert)
                .orElseThrow(() -> new AccountNotFoundException("Cannot find account by account number: " + accountNumber));
    }

    @Transactional
    @Override
    public Account createAccount(CreateAccountDto accountDto) {
        log.debug("Creating account: {}", accountDto);
        AccountEntity accountEntity = generateAccountNumber(Objects.requireNonNull(toAccountEntityConverter.convert(accountDto)));
        Account account = toAccountConverter.convert(accountRepository.save(accountEntity));
        log.debug("Successfully saved account: {}", account);
        return account;
    }

    private AccountEntity generateAccountNumber(AccountEntity accountEntity) {
        return accountEntity.toBuilder()
                .accountNumber(AccountUtils.generateAccountNumber())
                .build();
    }
}
