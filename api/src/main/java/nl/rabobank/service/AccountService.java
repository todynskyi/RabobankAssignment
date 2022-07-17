package nl.rabobank.service;

import nl.rabobank.account.Account;
import nl.rabobank.model.CreateAccountDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccountService {

    List<Account> getAccounts(Pageable page);

    Account getAccount(String accountNumber);

    Account createAccount(CreateAccountDto accountDto);
}
