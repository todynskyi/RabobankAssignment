package nl.rabobank.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.rabobank.account.AccountType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountDto {

    @Schema(example = "John Due")
    @NotBlank
    private String accountHolderName;
    @Schema(example = "100")
    @NotNull
    @PositiveOrZero
    private Double initialBalance;
    @NotNull
    private AccountType type;
}
