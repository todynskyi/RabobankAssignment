package nl.rabobank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import nl.rabobank.account.Account;
import nl.rabobank.account.AccountType;
import nl.rabobank.exception.AccountNotFoundException;
import nl.rabobank.model.CreateAccountDto;
import nl.rabobank.model.ErrorDetails;
import nl.rabobank.mongo.MongoConfiguration;
import nl.rabobank.service.AccountService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static nl.rabobank.util.AccountTestDataUtils.createAccountDto;
import static nl.rabobank.util.AccountTestDataUtils.createAccounts;
import static nl.rabobank.util.AccountTestDataUtils.toAccount;
import static nl.rabobank.util.ValidationUtils.NOT_BLANK_VALIDATION_MESSAGE;
import static nl.rabobank.util.ValidationUtils.NOT_NULL_VALIDATION_MESSAGE;
import static nl.rabobank.util.ValidationUtils.POSITIVE_OR_ZERO_VALIDATION_MESSAGE;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AccountService accountService;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private MongoConfiguration mongoProperties;

    @DisplayName("Should create Payment and Savings accounts")
    @SneakyThrows
    @ParameterizedTest(name = "{index} Created Account Type: {0}")
    @EnumSource(value = AccountType.class)
    public void shouldCreateAccounts(AccountType type) {
        CreateAccountDto accountDto = createAccountDto(type);
        Account account = toAccount(accountDto);
        Mockito.when(accountService.createAccount(accountDto)).thenReturn(account);

        mockMvc.perform(post("/api/v1/accounts").content(objectMapper.writeValueAsBytes(accountDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().bytes(objectMapper.writeValueAsBytes(account)))
                .andDo(print());
    }

    @DisplayName("Should not create account and return BadRequest in case of missing all account attributes")
    @SneakyThrows
    @Test
    public void shouldNotCreateAccountAndReturnBadRequestInCaseOfMissingAccountAttributes() {
        CreateAccountDto accountDto = new CreateAccountDto();

        mockMvc.perform(post("/api/v1/accounts").content(objectMapper.writeValueAsBytes(accountDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", Matchers.is(NOT_NULL_VALIDATION_MESSAGE)))
                .andExpect(jsonPath("$.initialBalance", Matchers.is(NOT_NULL_VALIDATION_MESSAGE)))
                .andExpect(jsonPath("$.accountHolderName", Matchers.is(NOT_BLANK_VALIDATION_MESSAGE)))
                .andDo(print());
    }

    @DisplayName("Should not create account and BadRequest in case of negative balance")
    @SneakyThrows
    @ParameterizedTest(name = "{index} Account Type: {0}")
    @EnumSource(value = AccountType.class)
    public void shouldNotCreateAccountAndReturnBadRequestInCaseNegativeBalance(AccountType accountType) {
        CreateAccountDto accountDto = createAccountDto(accountType);
        accountDto.setInitialBalance(-1.);

        mockMvc.perform(post("/api/v1/accounts").content(objectMapper.writeValueAsBytes(accountDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.initialBalance", Matchers.is(POSITIVE_OR_ZERO_VALIDATION_MESSAGE)))
                .andDo(print());
    }

    @DisplayName("Should return Account by accountNumber")
    @SneakyThrows
    @ParameterizedTest(name = "{index} Account Type: {0}")
    @EnumSource(value = AccountType.class)
    public void shouldReturnAccountsByAccountNumber(AccountType type) {
        Account account = toAccount(createAccountDto(type));
        Mockito.when(accountService.getAccount(account.getAccountNumber())).thenReturn(account);

        mockMvc.perform(get("/api/v1/accounts/{accountNumber}", account.getAccountNumber())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().bytes(objectMapper.writeValueAsBytes(account)))
                .andDo(print());
    }

    @DisplayName("Should return NotFound for not existed account")
    @SneakyThrows
    @Test
    public void shouldReturnNotFoundForNotExistedAccount() {
        ErrorDetails error = new ErrorDetails("Missing account");
        Mockito.when(accountService.getAccount(any())).thenThrow(new AccountNotFoundException(error.getMessage()));

        mockMvc.perform(get("/api/v1/accounts/{accountNumber}", "fake accountNumber")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().bytes(objectMapper.writeValueAsBytes(error)))
                .andDo(print());
    }

    @DisplayName("Should return Accounts")
    @SneakyThrows
    @Test
    public void shouldReturnAccounts() {
        List<Account> accounts = createAccounts();
        Mockito.when(accountService.getAccounts(any())).thenReturn(accounts);

        mockMvc.perform(get("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().bytes(objectMapper.writeValueAsBytes(accounts)))
                .andDo(print());
    }
}
