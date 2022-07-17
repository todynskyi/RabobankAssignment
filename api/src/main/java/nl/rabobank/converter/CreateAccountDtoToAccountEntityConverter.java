package nl.rabobank.converter;

import nl.rabobank.model.CreateAccountDto;
import nl.rabobank.mongo.model.AccountEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CreateAccountDtoToAccountEntityConverter implements Converter<CreateAccountDto, AccountEntity> {

    @Override
    public AccountEntity convert(CreateAccountDto account) {
        return AccountEntity.builder()
                .accountHolderName(account.getAccountHolderName())
                .balance(account.getInitialBalance())
                .type(account.getType())
                .build();
    }
}
