package nl.rabobank.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.rabobank.authorizations.Authorization;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePowerOfAttorneyDto {

    @Schema(example = "account number")
    @NotBlank
    private String accountNumber;
    @Schema(example = "John Due")
    @NotBlank
    private String grantor;
    @Schema(example = "Grantee")
    @NotBlank
    private String grantee;
    @NotNull
    private Authorization authorization;
}
