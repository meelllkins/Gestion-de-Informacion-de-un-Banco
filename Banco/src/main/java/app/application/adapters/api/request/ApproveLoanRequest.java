package app.application.adapters.api.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApproveLoanRequest {

    @Positive(message = "El monto aprobado debe ser mayor a cero")
    private double approvedAmount;

    @PositiveOrZero(message = "La tasa de interés no puede ser negativa")
    private double interestRate;
}
