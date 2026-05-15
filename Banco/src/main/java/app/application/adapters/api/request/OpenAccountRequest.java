package app.application.adapters.api.request;

import app.domain.models.enums.AccountType;
import app.domain.models.enums.Category;
import app.domain.models.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OpenAccountRequest {

    @NotBlank(message = "El número de cuenta es obligatorio")
    private String accountNumber;

    @NotNull(message = "El tipo de cuenta es obligatorio")
    private AccountType accountType;

    @NotBlank(message = "El ID del titular es obligatorio")
    private String accountHolderId;

    private double balance;

    @NotNull(message = "La moneda es obligatoria")
    private Currency currency;

    @NotBlank(message = "El código de producto es obligatorio")
    private String productCode;

    @NotBlank(message = "El nombre de producto es obligatorio")
    private String productName;

    @NotNull(message = "La categoría es obligatoria")
    private Category category;

    private boolean requiresApproval;
}
