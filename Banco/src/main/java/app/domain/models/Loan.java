package app.domain.models;

import java.time.LocalDate;

import app.domain.enums.LoanStatus;
import app.domain.enums.LoanType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class Loan {
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
}