package app.domain.services.implementations;

import app.domain.models.BankAccount;
import app.domain.models.Loan;
import app.domain.models.User;
import app.domain.models.enums.LoanStatus;
import app.domain.models.enums.SystemRole;
import app.domain.models.enums.UserStatus;
import app.domain.ports.IAccountPort;
import app.domain.ports.IUserPort;
import app.domain.services.interfaces.ILogService;
import app.domain.services.interfaces.ILoanService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LoanService implements ILoanService {

    private final IUserPort userPort;
    private final ILogService logService;
    private final GetActiveAccount getActiveAccount;
    private final IAccountPort accountPort;

    private final List<Loan> loans = new ArrayList<>();
    private int nextLoanId = 1;

    public LoanService(IUserPort userPort, ILogService logService,
                       GetActiveAccount getActiveAccount, IAccountPort accountPort) {
        this.userPort = userPort;
        this.logService = logService;
        this.getActiveAccount = getActiveAccount;
        this.accountPort = accountPort;
    }

    @Override
    public Loan requestLoan(Loan loan, User requestingUser) {
        User client = userPort.findByIdentificationId(loan.getApplicantClientId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un cliente con ID: " + loan.getApplicantClientId()));

        if (client.getUserStatus() == UserStatus.INACTIVE || client.getUserStatus() == UserStatus.BLOCKED) {
            throw new IllegalStateException(
                "El cliente " + loan.getApplicantClientId() + " no está activo.");
        }

        if (loan.getRequestedAmount() <= 0) {
            throw new IllegalArgumentException("El monto solicitado debe ser mayor a cero.");
        }
        if (loan.getTermMonths() <= 0) {
            throw new IllegalArgumentException("El plazo en meses debe ser mayor a cero.");
        }
        if (loan.getLoanType() == null) {
            throw new IllegalArgumentException("El tipo de préstamo es obligatorio.");
        }

        loan.setLoanId(nextLoanId++);
        loan.setLoanStatus(LoanStatus.PENDING);
        loan.setApprovedAmount(0);
        loan.setInterestRate(0);
        loans.add(loan);

        Map<String, Object> detail = new HashMap<>();
        detail.put("clientId", loan.getApplicantClientId());
        detail.put("requestedAmount", loan.getRequestedAmount());
        detail.put("loanType", loan.getLoanType().toString());
        detail.put("termMonths", loan.getTermMonths());
        logService.log("LOAN_REQUESTED", requestingUser, String.valueOf(loan.getLoanId()), detail);

        return loan;
    }

    @Override
    public Loan approveLoan(int loanId, double approvedAmount, double interestRate, User analystUser) {
        Loan loan = getLoanById(loanId);

        if (loan.getLoanStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException(
                "Solo se puede aprobar un préstamo en estado PENDING. Estado actual: " + loan.getLoanStatus());
        }
        if (approvedAmount <= 0) {
            throw new IllegalArgumentException("El monto aprobado debe ser mayor a cero.");
        }
        if (interestRate < 0) {
            throw new IllegalArgumentException("La tasa de interés no puede ser negativa.");
        }

        LoanStatus previousStatus = loan.getLoanStatus();
        loan.setLoanStatus(LoanStatus.APPROVED);
        loan.setApprovedAmount(approvedAmount);
        loan.setInterestRate(interestRate);
        loan.setApprovalDate(LocalDate.now());

        Map<String, Object> detail = new HashMap<>();
        detail.put("previousStatus", previousStatus.toString());
        detail.put("newStatus", LoanStatus.APPROVED.toString());
        detail.put("approvedAmount", approvedAmount);
        detail.put("interestRate", interestRate);
        detail.put("analystId", analystUser.getIdentificationId());
        logService.log("LOAN_APPROVED", analystUser, String.valueOf(loanId), detail);

        return loan;
    }

    @Override
    public Loan rejectLoan(int loanId, User analystUser) {
        Loan loan = getLoanById(loanId);

        if (loan.getLoanStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException(
                "Solo se puede rechazar un préstamo en estado PENDING. Estado actual: " + loan.getLoanStatus());
        }

        LoanStatus previousStatus = loan.getLoanStatus();
        loan.setLoanStatus(LoanStatus.REJECTED);

        Map<String, Object> detail = new HashMap<>();
        detail.put("previousStatus", previousStatus.toString());
        detail.put("newStatus", LoanStatus.REJECTED.toString());
        detail.put("analystId", analystUser.getIdentificationId());
        logService.log("LOAN_REJECTED", analystUser, String.valueOf(loanId), detail);

        return loan;
    }

    @Override
    public Loan disburseLoan(int loanId, User analystUser) {
        Loan loan = getLoanById(loanId);

        if (loan.getLoanStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException(
                "Solo se puede desembolsar un préstamo en estado APPROVED. Estado actual: " + loan.getLoanStatus());
        }

        if (loan.getDestinationAccount() == null || loan.getDestinationAccount().isBlank()) {
            throw new IllegalArgumentException(
                "Debe definir la Cuenta_Destino_Desembolso antes de desembolsar.");
        }

        BankAccount destAccount = getActiveAccount.getActiveAccount(loan.getDestinationAccount());

        if (loan.getApprovedAmount() <= 0) {
            throw new IllegalStateException("El monto aprobado debe ser mayor a cero para desembolsar.");
        }

        double balanceBefore = destAccount.getBalance();
        double newBalance = balanceBefore + loan.getApprovedAmount();
        accountPort.updateBalance(loan.getDestinationAccount(), newBalance);

        loan.setLoanStatus(LoanStatus.DISBURSED);
        loan.setDisbursementDate(LocalDate.now());

        Map<String, Object> detail = new HashMap<>();
        detail.put("previousStatus", LoanStatus.APPROVED.toString());
        detail.put("newStatus", LoanStatus.DISBURSED.toString());
        detail.put("disbursedAmount", loan.getApprovedAmount());
        detail.put("destinationAccount", loan.getDestinationAccount());
        detail.put("balanceBefore", balanceBefore);
        detail.put("balanceAfter", newBalance);
        detail.put("analystId", analystUser.getIdentificationId());
        logService.log("LOAN_DISBURSED", analystUser, String.valueOf(loanId), detail);

        return loan;
    }

    @Override
    public Loan findById(int loanId, User requestingUser) {
        Loan loan = getLoanById(loanId);

        SystemRole role = requestingUser.getSystemRole();
        if (role == SystemRole.INDIVIDUAL_CUSTOMER || role == SystemRole.CORPORATE_CUSTOMER) {
            if (!loan.getApplicantClientId().equals(requestingUser.getIdentificationId())) {
                throw new SecurityException("No tiene permiso para ver este préstamo.");
            }
        }
        return loan;
    }

    @Override
    public List<Loan> getLoansByClient(String clientId, User requestingUser) {
        SystemRole role = requestingUser.getSystemRole();
        if (role == SystemRole.INDIVIDUAL_CUSTOMER || role == SystemRole.CORPORATE_CUSTOMER) {
            if (!clientId.equals(requestingUser.getIdentificationId())) {
                throw new SecurityException("No tiene permiso para ver préstamos de otro cliente.");
            }
        }
        return loans.stream()
                .filter(l -> l.getApplicantClientId().equals(clientId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Loan> getPendingLoans(User analystUser) {
        return loans.stream()
                .filter(l -> l.getLoanStatus() == LoanStatus.PENDING)
                .collect(Collectors.toList());
    }

    private Loan getLoanById(int loanId) {
        return loans.stream()
                .filter(l -> l.getLoanId() == loanId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Préstamo no encontrado: #" + loanId));
    }
}