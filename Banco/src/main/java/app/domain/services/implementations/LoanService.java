package app.domain.services.implementations;

import app.domain.models.BankAccount;
import app.domain.models.Loan;
import app.domain.models.User;
import app.domain.models.enums.LoanStatus;
import app.domain.models.enums.SystemRole;
import app.domain.models.enums.UserStatus;
import app.domain.services.interfaces.IAuthService;
import app.domain.services.interfaces.ILogService;
import app.domain.services.interfaces.ILoanService;
import app.domain.services.interfaces.IUserService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class LoanService implements ILoanService {

    private final List<Loan> loans = new ArrayList<>();
    private final IAuthService authService;
    private final IUserService userService;
    private final ILogService logService;
    private final AccountService accountService; // Para modificar saldos en el desembolso

    private int nextLoanId = 1;

    public LoanService(IAuthService authService, IUserService userService,
                       ILogService logService, AccountService accountService) {
        this.authService = authService;
        this.userService = userService;
        this.logService = logService;
        this.accountService = accountService;
    }

    @Override
    public Loan requestLoan(Loan loan, User requestingUser) {
        // Roles permitidos para solicitar un préstamo
        authService.validateRole(requestingUser,
                SystemRole.INDIVIDUAL_CUSTOMER,
                SystemRole.CORPORATE_CUSTOMER,
                SystemRole.COMMERCIAL_EMPLOYEE);

        // Validar que el cliente solicitante exista y esté activo
        User client = userService.findByIdentificationId(loan.getApplicantClientId());
        if (client.getUserStatus() == UserStatus.INACTIVE || client.getUserStatus() == UserStatus.BLOCKED) {
            throw new IllegalStateException(
                "El cliente " + loan.getApplicantClientId() + " no está activo.");
        }

        // Validaciones de campos
        if (loan.getRequestedAmount() <= 0) {
            throw new IllegalArgumentException("El monto solicitado debe ser mayor a cero.");
        }
        if (loan.getTermMonths() <= 0) {
            throw new IllegalArgumentException("El plazo en meses debe ser mayor a cero.");
        }
        if (loan.getLoanType() == null) {
            throw new IllegalArgumentException("El tipo de préstamo es obligatorio.");
        }

        // Estado inicial obligatorio: PENDING
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

        System.out.println("Solicitud de préstamo #" + loan.getLoanId() + " creada. Estado: PENDING");
        return loan;
    }

    @Override
    public Loan approveLoan(int loanId, double approvedAmount, double interestRate, User analystUser) {
        // Solo el Analista Interno puede aprobar
        authService.validateRole(analystUser, SystemRole.INTERNAL_ANALYST);

        Loan loan = getLoanById(loanId);

        // Regla de transición: solo se puede aprobar desde PENDING
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

        System.out.println("Préstamo #" + loanId + " APROBADO. Monto: " + approvedAmount);
        return loan;
    }

    @Override
    public Loan rejectLoan(int loanId, User analystUser) {
        authService.validateRole(analystUser, SystemRole.INTERNAL_ANALYST);

        Loan loan = getLoanById(loanId);

        // Regla de transición: solo se puede rechazar desde PENDING
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

        System.out.println("Préstamo #" + loanId + " RECHAZADO.");
        return loan;
    }

    @Override
    public Loan disburseLoan(int loanId, User analystUser) {
        authService.validateRole(analystUser, SystemRole.INTERNAL_ANALYST);

        Loan loan = getLoanById(loanId);

        // Regla de transición: solo se puede desembolsar desde APPROVED
        if (loan.getLoanStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException(
                "Solo se puede desembolsar un préstamo en estado APPROVED. Estado actual: " + loan.getLoanStatus());
        }

        // Validar que la cuenta destino esté definida
        if (loan.getDestinationAccount() == null || loan.getDestinationAccount().isBlank()) {
            throw new IllegalArgumentException(
                "Debe definir la Cuenta_Destino_Desembolso antes de desembolsar.");
        }

        // Validar que la cuenta destino exista y esté activa
        BankAccount destAccount = accountService.getActiveAccount(loan.getDestinationAccount());

        // Validar monto aprobado > 0
        if (loan.getApprovedAmount() <= 0) {
            throw new IllegalStateException("El monto aprobado debe ser mayor a cero para desembolsar.");
        }

        // Impacto financiero: aumentar saldo de la cuenta destino
        double balanceBefore = destAccount.getBalance();
        destAccount.setBalance(destAccount.getBalance() + loan.getApprovedAmount());

        loan.setLoanStatus(LoanStatus.DISBURSED);
        loan.setDisbursementDate(LocalDate.now());

        Map<String, Object> detail = new HashMap<>();
        detail.put("previousStatus", LoanStatus.APPROVED.toString());
        detail.put("newStatus", LoanStatus.DISBURSED.toString());
        detail.put("disbursedAmount", loan.getApprovedAmount());
        detail.put("destinationAccount", loan.getDestinationAccount());
        detail.put("balanceBefore", balanceBefore);
        detail.put("balanceAfter", destAccount.getBalance());
        detail.put("analystId", analystUser.getIdentificationId());
        logService.log("LOAN_DISBURSED", analystUser, String.valueOf(loanId), detail);

        System.out.printf("Préstamo #%d DESEMBOLSADO. %.2f abonados a cuenta %s.%n",
                loanId, loan.getApprovedAmount(), loan.getDestinationAccount());
        return loan;
    }

    @Override
    public Loan findById(int loanId, User requestingUser) {
        Loan loan = getLoanById(loanId);

        // Clientes solo pueden ver sus propios préstamos
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
        // Clientes solo pueden ver los suyos
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
        authService.validateRole(analystUser, SystemRole.INTERNAL_ANALYST);
        return loans.stream()
                .filter(l -> l.getLoanStatus() == LoanStatus.PENDING)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    // Métodos privados
    // ──────────────────────────────────────────────

    private Loan getLoanById(int loanId) {
        return loans.stream()
                .filter(l -> l.getLoanId() == loanId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Préstamo no encontrado: #" + loanId));
    }
}
