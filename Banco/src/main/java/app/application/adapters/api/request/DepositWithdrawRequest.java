package app.application.adapters.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DepositWithdrawRequest {

    @NotBlank(message = "El número de cuenta no puede estar vacío")
    private String accountNumber;

    @Positive(message = "El monto debe ser mayor a cero")
    private double amount;
}
