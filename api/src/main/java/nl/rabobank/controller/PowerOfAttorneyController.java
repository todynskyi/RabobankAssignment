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
import nl.rabobank.authorizations.Authorization;
import nl.rabobank.authorizations.PowerOfAttorney;
import nl.rabobank.model.CreatePowerOfAttorneyDto;
import nl.rabobank.model.ErrorDetails;
import nl.rabobank.service.PowerOfAttorneyService;
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
import static nl.rabobank.model.ResponseCode.UNAUTHORIZED;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Tag(name = "Power Of Attorneys")
@RestController
@RequestMapping("api/v1/power-of-attorneys")
@RequiredArgsConstructor
public class PowerOfAttorneyController {

    private final PowerOfAttorneyService powerOfAttorneyService;

    @Operation(summary = "Grants access to account")
    @ApiResponses({
            @ApiResponse(responseCode = CREATED, description = "Account access is granted"),
            @ApiResponse(responseCode = BAD_REQUEST, description = "Validation/conversion is failed",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = UNAUTHORIZED, description = "Unauthorized request",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = NOT_FOUND, description = "Account not found",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            )
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public PowerOfAttorney grantAccess(@RequestBody @Valid CreatePowerOfAttorneyDto powerOfAttorneyDto) {
        return powerOfAttorneyService.grantAccess(powerOfAttorneyDto);
    }

    @Operation(summary = "Returns granted accounts for the particular grantee")
    @ApiResponses({
            @ApiResponse(responseCode = OK, description = "Returns list of granted accounts")
    })
    @GetMapping("{granteeName}/accounts")
    public List<Account> getGrantedAccounts(
            @Parameter(description = "Grantee Name") @PathVariable("granteeName") String granteeName) {
        return powerOfAttorneyService.getGrantedAccounts(granteeName);
    }

    @Operation(summary = "Returns granted accounts for the particular grantee and authorization")
    @ApiResponses({
            @ApiResponse(responseCode = OK, description = "Returns list of granted accounts")
    })
    @GetMapping("{granteeName}/accounts/{authorization}")
    public List<Account> getGrantedAccounts(
            @Parameter(description = "Grantee Name") @PathVariable("granteeName") String granteeName,
            @Parameter(description = "Authorization Read/Write") @PathVariable("authorization") Authorization authorization) {
        return powerOfAttorneyService.getGrantedAccounts(granteeName, authorization);
    }

    @Operation(summary = "Returns Power Of Attorneys for the particular grantee")
    @ApiResponses({
            @ApiResponse(responseCode = OK, description = "Returns list of granted accounts")
    })
    @GetMapping("{granteeName}")
    public List<PowerOfAttorney> getPowerOfAttorneys(
            @Parameter(description = "Grantee Name") @PathVariable("granteeName") String granteeName) {
        return powerOfAttorneyService.getPowerOfAttorneys(granteeName);
    }
}
