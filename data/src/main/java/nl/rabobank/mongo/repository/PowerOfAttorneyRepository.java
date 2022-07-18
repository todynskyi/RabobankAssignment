package nl.rabobank.mongo.repository;


import nl.rabobank.authorizations.Authorization;
import nl.rabobank.mongo.model.PowerOfAttorneyEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PowerOfAttorneyRepository extends MongoRepository<PowerOfAttorneyEntity, String> {

    List<PowerOfAttorneyEntity> findByGranteeOrderById(String grantee);

    List<PowerOfAttorneyEntity> findByGranteeAndAuthorizationOrderById(String grantee, Authorization authorization);
}
