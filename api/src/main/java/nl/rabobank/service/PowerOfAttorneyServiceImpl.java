package nl.rabobank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.rabobank.account.Account;
import nl.rabobank.authorizations.Authorization;
import nl.rabobank.authorizations.PowerOfAttorney;
import nl.rabobank.exception.AccountNotFoundException;
import nl.rabobank.exception.PowerOfAttorneySecurityException;
import nl.rabobank.model.CreatePowerOfAttorneyDto;
import nl.rabobank.mongo.model.AccountEntity;
import nl.rabobank.mongo.model.PowerOfAttorneyGrantee;
import nl.rabobank.mongo.repository.AccountRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PowerOfAttorneyServiceImpl implements PowerOfAttorneyService {

    private final AccountRepository accountRepository;
    private final Converter<CreatePowerOfAttorneyDto, PowerOfAttorneyGrantee> toPowerOfAttorneyGranteeConverter;
    private final Converter<AccountEntity, Account> toAccountConverter;
    private final Converter<Pair<List<AccountEntity>, String>, List<PowerOfAttorney>> toPowerOfAttorneys;
    private final Converter<Pair<CreatePowerOfAttorneyDto, AccountEntity>, PowerOfAttorney> toPowerOfAttorney;

    @Transactional
    @Override
    public PowerOfAttorney grantAccess(CreatePowerOfAttorneyDto powerOfAttorneyDto) {
        log.debug("Creating Power Of Attorney: {}", powerOfAttorneyDto);

        AccountEntity account = getAccount(powerOfAttorneyDto);
        account.addGrantee(toPowerOfAttorneyGranteeConverter.convert(powerOfAttorneyDto));
        AccountEntity savedAccount = accountRepository.save(account);

        log.debug("{} successfully obtained access to account number: {}", powerOfAttorneyDto.getGrantee(), powerOfAttorneyDto.getAccountNumber());
        return toPowerOfAttorney.convert(Pair.of(powerOfAttorneyDto, savedAccount));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Account> getGrantedAccounts(String granteeName) {
        log.debug("Fetching Granted Accounts by Grantee: {}", granteeName);
        return accountRepository.findAccountEntityByGrantees_GranteeNameOrderById(granteeName)
                .stream()
                .map(toAccountConverter::convert)
                .collect(Collectors.toList());
    }

    @Override
    public List<Account> getGrantedAccounts(final String granteeName, final Authorization authorization) {
        log.debug("Fetching Granted Accounts by Grantee: {}", granteeName);
        return accountRepository.findAccountEntityByGrantees_GranteeNameAndGrantees_AuthorizationOrderById(granteeName, authorization)
                .stream()
                .map(toAccountConverter::convert)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<PowerOfAttorney> getPowerOfAttorneys(String granteeName) {
        log.debug("Fetching Power Of Attorneys by Grantee: {}", granteeName);
        List<AccountEntity> accounts = accountRepository.findAccountEntityByGrantees_GranteeNameOrderById(granteeName);
        return toPowerOfAttorneys.convert(Pair.of(accounts, granteeName));
    }

    private AccountEntity getAccount(CreatePowerOfAttorneyDto powerOfAttorneyDto) {
        if (powerOfAttorneyDto.getGrantor().equals(powerOfAttorneyDto.getGrantee())) {
            throw new PowerOfAttorneySecurityException("Grantor cannot give access to himself/herself");
        }
        var account = accountRepository.findAccountEntityByAccountNumber(powerOfAttorneyDto.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Cannot find account by account number: " + powerOfAttorneyDto.getAccountNumber()));

        if (!powerOfAttorneyDto.getGrantor().equals(account.getAccountHolderName())) {
            throw new PowerOfAttorneySecurityException("Grantor can give access only for own account");
        }

        return account;
    }
}
