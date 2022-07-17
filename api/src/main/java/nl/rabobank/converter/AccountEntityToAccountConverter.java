package nl.rabobank.converter;

import nl.rabobank.account.Account;
import nl.rabobank.account.AccountType;
import nl.rabobank.account.PaymentAccount;
import nl.rabobank.account.SavingsAccount;
import nl.rabobank.mongo.model.AccountEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AccountEntityToAccountConverter implements Converter<AccountEntity, Account> {

    @Override
    public Account convert(AccountEntity account) {
        if (account.getType() == AccountType.PAYMENT) {
            return new PaymentAccount(account.getAccountNumber(), account.getAccountHolderName(), account.getBalance());
        }
        return new SavingsAccount(account.getAccountNumber(), account.getAccountHolderName(), account.getBalance());
    }
}
