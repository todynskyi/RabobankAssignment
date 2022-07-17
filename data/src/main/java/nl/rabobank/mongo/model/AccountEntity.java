package nl.rabobank.mongo.model;

import lombok.Builder;
import lombok.Data;
import nl.rabobank.account.AccountType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder(toBuilder = true)
@Document("accounts")
public class AccountEntity {

    @Id
    private String id;
    @Indexed(unique = true)
    private String accountNumber;
    private String accountHolderName;
    private Double balance;
    private AccountType type;
    @Indexed
    private Set<PowerOfAttorneyGrantee> grantees;

    public AccountEntity addGrantee(PowerOfAttorneyGrantee grantee) {
        if (grantees == null) {
            grantees = new HashSet<>();
        }
        grantees.add(grantee);
        return this;
    }
}
