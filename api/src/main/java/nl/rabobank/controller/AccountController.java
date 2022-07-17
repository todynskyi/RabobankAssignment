package nl.rabobank.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nl.rabobank.account.Account;
import nl.rabobank.model.CreateAccountDto;
import nl.rabobank.model.ErrorDetails;
import nl.rabobank.service.AccountService;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static nl.rabobank.model.ResponseCode.BAD_REQUEST;
import static nl.rabobank.model.ResponseCode.CREATED;
import static nl.rabobank.model.ResponseCode.NOT_FOUND;
import static nl.rabobank.model.ResponseCode.OK;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Tag(name = "Accounts")
@RestController
@RequestMapping("api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Returns Accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = OK, description = "Returns list of accounts")
    })
    @GetMapping
    public List<Account> getAccounts(@ParameterObject @PageableDefault(size = 20) Pageable page) {
        return accountService.getAccounts(page);
    }

    @Operation(summary = "Returns Account by account number")
    @ApiResponses({
            @ApiResponse(responseCode = OK, description = "Account found"),
            @ApiResponse(responseCode = NOT_FOUND, description = "Account not found",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            )
    })
    @GetMapping("{accountNumber}")
    public Account getAccount(@Parameter(description = "Account Number") @PathVariable("accountNumber") String accountNumber) {
        return accountService.getAccount(accountNumber);
    }

    @Operation(summary = "Creates Account")
    @ApiResponses({
            @ApiResponse(responseCode = CREATED, description = "Account is created"),
            @ApiResponse(responseCode = BAD_REQUEST, description = "Validation/conversion is failed",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            )
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Account createAccount(@RequestBody @Valid CreateAccountDto accountDto) {
        return accountService.createAccount(accountDto);
    }
}
