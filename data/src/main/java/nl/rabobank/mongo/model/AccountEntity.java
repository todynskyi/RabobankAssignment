package nl.rabobank.mongo.model;

import lombok.Builder;
import lombok.Data;
import nl.rabobank.account.Account;
import nl.rabobank.account.AccountType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder(toBuilder = true)
@Document("accounts")
public class AccountEntity implements Account {

    @Id
    private String id;
    @Indexed(unique = true)
    private String accountNumber;
    private String accountHolderName;
    private Double balance;
    private AccountType type;
}
