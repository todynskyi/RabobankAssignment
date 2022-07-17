package nl.rabobank.mongo.repository;

import nl.rabobank.authorizations.Authorization;
import nl.rabobank.mongo.model.AccountEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends MongoRepository<AccountEntity, String> {

    Optional<AccountEntity> findAccountEntityByAccountNumber(String accountNumber);

    List<AccountEntity> findAccountEntityByGrantees_GranteeNameOrderById(String granteeName);

    List<AccountEntity> findAccountEntityByGrantees_GranteeNameAndGrantees_AuthorizationOrderById(String granteeName, Authorization authorization);
}
