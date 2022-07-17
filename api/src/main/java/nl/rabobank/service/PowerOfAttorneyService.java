package nl.rabobank.service;

import nl.rabobank.account.Account;
import nl.rabobank.authorizations.PowerOfAttorney;
import nl.rabobank.model.CreatePowerOfAttorneyDto;

import java.util.List;

public interface PowerOfAttorneyService {

    PowerOfAttorney grantAccess(CreatePowerOfAttorneyDto powerOfAttorneyDto);

    List<Account> getGrantedAccounts(String granteeName);

    List<PowerOfAttorney> getPowerOfAttorneys(String granteeName);
}
