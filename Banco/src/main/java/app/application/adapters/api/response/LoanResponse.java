package app.application.adapters.api.response;

import app.domain.models.enums.Category;
import app.domain.models.enums.LoanStatus;
import app.domain.models.enums.LoanType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class LoanResponse {
    private int loanId;
    private LoanType loanType;
    private String applicantClientId;
    private double requestedAmount;
    private double approvedAmount;
    private double interestRate;
    private int termMonths;
    private LocalDate approvalDate;
    private LocalDate disbursementDate;
    private String destinationAccount;
    private LoanStatus loanStatus;
    private String productCode;
    private String productName;
    private Category category;
}
