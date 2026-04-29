package app.application.adapters.persistance.sql.entities;

import app.domain.models.enums.Category;
import app.domain.models.enums.LoanStatus;
import app.domain.models.enums.LoanType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
public class LoanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "product_name")
    private String productName;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "requires_approval")
    private boolean requiresApproval;

    @Column(name = "loan_id", nullable = false, unique = true)
    private int loanId;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false)
    private LoanType loanType;

    @Column(name = "applicant_client_id", nullable = false)
    private String applicantClientId;

    @Column(name = "requested_amount", nullable = false)
    private double requestedAmount;

    @Column(name = "approved_amount")
    private double approvedAmount;

    @Column(name = "interest_rate")
    private double interestRate;

    @Column(name = "term_months", nullable = false)
    private int termMonths;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @Column(name = "destination_account")
    private String destinationAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_status", nullable = false)
    private LoanStatus loanStatus;
}