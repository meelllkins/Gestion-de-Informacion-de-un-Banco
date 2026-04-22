package app.domain.ports;

import app.domain.models.Loan;
import app.domain.models.enums.LoanStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida — Repositorio de Préstamos.
 * Persiste en MySQL.
 * Las transiciones de estado siguen el flujo:
 * PENDING → APPROVED / REJECTED → DISBURSED
 */
public interface ILoanPort {

    /** Guarda un nuevo préstamo (estado inicial: PENDING). */
    void save(Loan loan);

    /** Busca un préstamo por su ID. */
    Optional<Loan> findById(int loanId);

    /** Retorna todos los préstamos de un cliente. */
    List<Loan> findByClientId(String clientId);

    /** Retorna todos los préstamos en un estado específico. */
    List<Loan> findByStatus(LoanStatus status);

    /**
     * Actualiza el estado del préstamo.
     * También registra la fecha de aprobación o desembolso según corresponda.
     */
    void updateStatus(int loanId, LoanStatus newStatus, LocalDate eventDate);

    /**
     * Actualiza el monto aprobado y la tasa de interés al momento de aprobar.
     * Solo se llama cuando el estado cambia a APPROVED.
     */
    void updateApprovalData(int loanId, double approvedAmount, double interestRate);

    /**
     * Registra la cuenta destino del desembolso.
     * Solo se llama cuando el estado cambia a DISBURSED.
     */
    void updateDestinationAccount(int loanId, String destinationAccount);
}
