package app.application.adapters.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateTransferRequest {

    @NotBlank(message = "La cuenta origen es obligatoria")
    private String sourceAccount;

    @NotBlank(message = "La cuenta destino es obligatoria")
    private String destinationAccount;

    @Positive(message = "El monto debe ser mayor a cero")
    private double amount;
}
