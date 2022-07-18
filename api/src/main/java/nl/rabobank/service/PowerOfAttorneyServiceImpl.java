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
import nl.rabobank.mongo.model.PowerOfAttorneyEntity;
import nl.rabobank.mongo.repository.AccountRepository;
import nl.rabobank.mongo.repository.PowerOfAttorneyRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@RequiredArgsConstructor
@Service
public class PowerOfAttorneyServiceImpl implements PowerOfAttorneyService {

    private final AccountRepository accountRepository;
    private final PowerOfAttorneyRepository powerOfAttorneyRepository;
    private final Converter<AccountEntity, Account> toAccountConverter;
    private final Converter<Pair<CreatePowerOfAttorneyDto, AccountEntity>, PowerOfAttorneyEntity> toPowerOfAttorneyEntity;
    private final Converter<PowerOfAttorneyEntity, PowerOfAttorney> toPowerOfAttorney;

    @Transactional
    @Override
    public PowerOfAttorney grantAccess(CreatePowerOfAttorneyDto powerOfAttorneyDto) {
        log.debug("Creating Power Of Attorney: {}", powerOfAttorneyDto);
        AccountEntity account = getAccount(powerOfAttorneyDto);
        PowerOfAttorneyEntity powerOfAttorneyEntity = requireNonNull(toPowerOfAttorneyEntity.convert(Pair.of(powerOfAttorneyDto, account)));
        PowerOfAttorneyEntity savedPowerOfAttorneyEntity = powerOfAttorneyRepository.save(powerOfAttorneyEntity);
        log.debug("{} successfully obtained access to account number: {}", powerOfAttorneyDto.getGrantee(), powerOfAttorneyDto.getAccountNumber());
        return toPowerOfAttorney.convert(savedPowerOfAttorneyEntity);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Account> getGrantedAccounts(String granteeName) {
        log.debug("Fetching Granted Accounts by Grantee: {}", granteeName);
        return powerOfAttorneyRepository.findByGranteeOrderById(granteeName)
                .stream()
                .map(PowerOfAttorneyEntity::getAccount)
                .distinct()
                .map(toAccountConverter::convert)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<Account> getGrantedAccounts(final String granteeName, final Authorization authorization) {
        log.debug("Fetching Granted Accounts by Grantee: {}", granteeName);
        return powerOfAttorneyRepository.findByGranteeAndAuthorizationOrderById(granteeName, authorization)
                .stream()
                .map(PowerOfAttorneyEntity::getAccount)
                .map(toAccountConverter::convert)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<PowerOfAttorney> getPowerOfAttorneys(String granteeName) {
        log.debug("Fetching Power Of Attorneys by Grantee: {}", granteeName);
        return powerOfAttorneyRepository.findByGranteeOrderById(granteeName)
                .stream()
                .map(toPowerOfAttorney::convert)
                .collect(Collectors.toList());
    }

    private AccountEntity getAccount(CreatePowerOfAttorneyDto powerOfAttorneyDto) {
        if (powerOfAttorneyDto.getGrantor().equals(powerOfAttorneyDto.getGrantee())) {
            throw new PowerOfAttorneySecurityException("Grantor cannot give access to himself/herself");
        }
        AccountEntity account = accountRepository.findAccountEntityByAccountNumber(powerOfAttorneyDto.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Cannot find account by account number: " + powerOfAttorneyDto.getAccountNumber()));

        if (!powerOfAttorneyDto.getGrantor().equals(account.getAccountHolderName())) {
            throw new PowerOfAttorneySecurityException("Grantor can give access only for own account");
        }

        return account;
    }
}
