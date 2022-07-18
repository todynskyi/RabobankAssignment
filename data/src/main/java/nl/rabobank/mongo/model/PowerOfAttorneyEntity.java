package nl.rabobank.mongo.model;

import lombok.Builder;
import lombok.Data;
import nl.rabobank.authorizations.Authorization;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import static nl.rabobank.mongo.util.MongoUtils.POWER_OF_ATTORNEY_UNIQUE_INDEX_NAME;

@Data
@Builder(toBuilder = true)
@CompoundIndexes(
        value = {@CompoundIndex(name = POWER_OF_ATTORNEY_UNIQUE_INDEX_NAME, def = "{'account.id': 1, 'grantee': 1, 'authorization': 1}", unique = true)}
)
@Document("power_of_attorneys")
public class PowerOfAttorneyEntity {

    @Id
    private String id;
    private String grantor;
    private String grantee;
    private Authorization authorization;
    @DBRef
    private AccountEntity account;
}
