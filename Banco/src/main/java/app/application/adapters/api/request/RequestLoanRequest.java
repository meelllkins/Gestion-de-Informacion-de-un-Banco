package app.application.adapters.api.request;

import app.domain.models.enums.Category;
import app.domain.models.enums.LoanType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RequestLoanRequest {

    @NotNull(message = "El tipo de préstamo es obligatorio")
    private LoanType loanType;

    @Positive(message = "El monto solicitado debe ser mayor a cero")
    private double requestedAmount;

    @Positive(message = "El plazo en meses debe ser mayor a cero")
    private int termMonths;

    private String productCode;
    private String productName;
    private Category category;
    private String destinationAccount;
}
