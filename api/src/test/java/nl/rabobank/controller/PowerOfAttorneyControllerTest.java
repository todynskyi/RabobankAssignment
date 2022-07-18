package nl.rabobank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoWriteException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteError;
import lombok.SneakyThrows;
import nl.rabobank.account.Account;
import nl.rabobank.account.AccountType;
import nl.rabobank.authorizations.Authorization;
import nl.rabobank.authorizations.PowerOfAttorney;
import nl.rabobank.exception.AccountNotFoundException;
import nl.rabobank.exception.PowerOfAttorneySecurityException;
import nl.rabobank.model.CreatePowerOfAttorneyDto;
import nl.rabobank.model.ErrorDetails;
import nl.rabobank.service.PowerOfAttorneyService;
import org.bson.BsonDocument;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static nl.rabobank.mongo.util.MongoUtils.POWER_OF_ATTORNEY_UNIQUE_INDEX_NAME;
import static nl.rabobank.util.AccountTestDataUtils.createAccounts;
import static nl.rabobank.util.PowerOfAttorneyTestDataUtils.createPowerOfAttorneyDto;
import static nl.rabobank.util.PowerOfAttorneyTestDataUtils.createPowerOfAttorneys;
import static nl.rabobank.util.PowerOfAttorneyTestDataUtils.toPowerOfAttorney;
import static nl.rabobank.util.ValidationUtils.NOT_BLANK_VALIDATION_MESSAGE;
import static nl.rabobank.util.ValidationUtils.NOT_NULL_VALIDATION_MESSAGE;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = PowerOfAttorneyController.class)
public class PowerOfAttorneyControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PowerOfAttorneyService powerOfAttorneyService;
    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("Should create Power Of Attorney for Saving Account")
    @SneakyThrows
    @ParameterizedTest(name = "{index} Authorization: {0} granted to Account Type: {1}")
    @MethodSource("nl.rabobank.util.PowerOfAttorneyTestDataUtils#source")
    public void shouldCreatePowerOfAttorneyForAccount(Authorization authorization, AccountType accountType) {
        CreatePowerOfAttorneyDto powerOfAttorneyDto = createPowerOfAttorneyDto(authorization);
        PowerOfAttorney powerOfAttorney = toPowerOfAttorney(powerOfAttorneyDto, accountType);
        Mockito.when(powerOfAttorneyService.grantAccess(powerOfAttorneyDto)).thenReturn(powerOfAttorney);

        mockMvc.perform(post("/api/v1/power-of-attorneys").content(objectMapper.writeValueAsBytes(powerOfAttorneyDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().bytes(objectMapper.writeValueAsBytes(powerOfAttorney)))
                .andDo(print());
    }

    @DisplayName("Should not create Power Of Attorney and return BadRequest in case of missing attributes")
    @SneakyThrows
    @Test
    public void shouldNotCreatePowerOfAttorneyAndReturnBadRequestInCaseOfMissingAttributes() {
        mockMvc.perform(post("/api/v1/power-of-attorneys").content(objectMapper.writeValueAsBytes(new CreatePowerOfAttorneyDto()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.accountNumber", Matchers.is(NOT_BLANK_VALIDATION_MESSAGE)))
                .andExpect(jsonPath("$.grantor", Matchers.is(NOT_BLANK_VALIDATION_MESSAGE)))
                .andExpect(jsonPath("$.grantee", Matchers.is(NOT_BLANK_VALIDATION_MESSAGE)))
                .andExpect(jsonPath("$.authorization", Matchers.is(NOT_NULL_VALIDATION_MESSAGE)))
                .andDo(print());
    }

    @DisplayName("Should not create Power Of Attorney and return NotFound in case of missing account")
    @SneakyThrows
    @ParameterizedTest(name = "{index} Authorization: {0}")
    @EnumSource(value = Authorization.class)
    public void shouldNotCreatePowerOfAttorneyAndReturnNotFoundInCaseOfMissingAccount(Authorization authorization) {
        ErrorDetails error = new ErrorDetails("Cannot find account");
        CreatePowerOfAttorneyDto powerOfAttorneyDto = createPowerOfAttorneyDto(authorization);
        Mockito.when(powerOfAttorneyService.grantAccess(powerOfAttorneyDto)).thenThrow(new AccountNotFoundException(error.getMessage()));

        mockMvc.perform(post("/api/v1/power-of-attorneys").content(objectMapper.writeValueAsBytes(powerOfAttorneyDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().bytes(objectMapper.writeValueAsBytes(error)))
                .andDo(print());
    }

    @DisplayName("Should not create Power Of Attorney and return NotFound in case of missing account")
    @SneakyThrows
    @Test
    public void shouldNotCreatePowerOfAttorneyAndReturnBadRequestInCaseOfConstraint() {
        ErrorDetails error = new ErrorDetails("Authorization already granted");
        Mockito.when(powerOfAttorneyService.grantAccess(any())).thenThrow(new MongoWriteException(new WriteError(1, POWER_OF_ATTORNEY_UNIQUE_INDEX_NAME, new BsonDocument()), new ServerAddress()));

        mockMvc.perform(post("/api/v1/power-of-attorneys").content(objectMapper.writeValueAsBytes(createPowerOfAttorneyDto(Authorization.WRITE)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes(objectMapper.writeValueAsBytes(error)))
                .andDo(print());
    }

    @DisplayName("Should not create Power Of Attorney and return Unauthorized in in case of granting not own account")
    @SneakyThrows
    @ParameterizedTest(name = "{index} Authorization: {0}")
    @EnumSource(value = Authorization.class)
    public void shouldNotCreatePowerOfAttorneyAndReturnNotFoundInCaseOfGrantingNotOwnAccount(Authorization authorization) {
        ErrorDetails error = new ErrorDetails("Grantor can give access only for own account");
        CreatePowerOfAttorneyDto powerOfAttorneyDto = createPowerOfAttorneyDto(authorization);
        Mockito.when(powerOfAttorneyService.grantAccess(powerOfAttorneyDto)).thenThrow(new PowerOfAttorneySecurityException(error.getMessage()));

        mockMvc.perform(post("/api/v1/power-of-attorneys").content(objectMapper.writeValueAsBytes(powerOfAttorneyDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().bytes(objectMapper.writeValueAsBytes(error)))
                .andDo(print());
    }

    @DisplayName("Should return Granted Accounts by Grantee")
    @SneakyThrows
    @Test
    public void shouldReturnGrantedAccounts() {
        String granteeName = "Grantee";
        List<Account> accounts = createAccounts(granteeName);
        Mockito.when(powerOfAttorneyService.getGrantedAccounts(granteeName)).thenReturn(accounts);

        mockMvc.perform(get("/api/v1/power-of-attorneys/{granteeName}/accounts", granteeName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().bytes(objectMapper.writeValueAsBytes(accounts)))
                .andDo(print());
    }

    @DisplayName("Should return Granted Accounts by Grantee")
    @SneakyThrows
    @ParameterizedTest(name = "{index} Authorization: {0}")
    @EnumSource(value = Authorization.class)
    public void shouldReturnGrantedAccountsByAuthorization(Authorization authorization) {
        String granteeName = "Grantee";
        List<Account> accounts = createAccounts(granteeName);
        Mockito.when(powerOfAttorneyService.getGrantedAccounts(granteeName, authorization)).thenReturn(accounts);

        mockMvc.perform(get("/api/v1/power-of-attorneys/{granteeName}/accounts/{authorization}", granteeName, authorization)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(accounts)))
                .andDo(print());
    }

    @DisplayName("Should return Power Of Attorneys by Grantee")
    @SneakyThrows
    @Test
    public void shouldReturnPowerOfAttorneys() {
        String granteeName = "Grantee";
        List<PowerOfAttorney> powerOfAttorneys = createPowerOfAttorneys(granteeName);
        Mockito.when(powerOfAttorneyService.getPowerOfAttorneys(granteeName)).thenReturn(powerOfAttorneys);

        mockMvc.perform(get("/api/v1/power-of-attorneys/{granteeName}", granteeName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().bytes(objectMapper.writeValueAsBytes(powerOfAttorneys)))
                .andDo(print());
    }
}
