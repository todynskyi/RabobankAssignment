package nl.rabobank.mongo.repository;

import nl.rabobank.mongo.model.AccountEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AccountRepository extends MongoRepository<AccountEntity, String> {

    Optional<AccountEntity> findAccountEntityByAccountNumber(String accountNumber);
}
