package app.domain.services.interfaces;

import app.domain.models.Loan;
import app.domain.models.User;

import java.util.List;

/**
 * Servicio de gestión de préstamos.
 * Implementa el flujo de aprobación completo: IN_STUDY → APPROVED/REJECTED → DISBURSED
 */
public interface ILoanService {

    /**
     * Crea una solicitud de préstamo.
     * Estado inicial: IN_STUDY.
     * Pueden hacerlo: Cliente Persona Natural, Cliente Empresa, Empleado Comercial.
     */
    Loan requestLoan(Loan loan, User requestingUser);

    /**
     * Aprueba una solicitud de préstamo.
     * Transición: IN_STUDY → APPROVED.
     * Solo puede hacerlo: Analista Interno.
     */
    Loan approveLoan(int loanId, double approvedAmount, double interestRate, User analystUser);

    /**
     * Rechaza una solicitud de préstamo.
     * Transición: IN_STUDY → REJECTED.
     * Solo puede hacerlo: Analista Interno.
     */
    Loan rejectLoan(int loanId, User analystUser);

    /**
     * Desembolsa un préstamo aprobado.
     * Transición: APPROVED → DISBURSED.
     * Reglas:
     * - Cuenta_Destino_Desembolso debe estar definida y ACTIVE.
     * - Monto_Aprobado > 0.
     * - Aumenta el saldo de la cuenta destino.
     * - Registra en Bitácora.
     * Solo puede hacerlo: Analista Interno.
     */
    Loan disburseLoan(int loanId, User analystUser);

    /**
     * Consulta un préstamo por su ID.
     * La visibilidad depende del rol del solicitante.
     */
    Loan findById(int loanId, User requestingUser);

    /**
     * Retorna todos los préstamos de un cliente.
     */
    List<Loan> getLoansByClient(String clientId, User requestingUser);

    /**
     * Retorna todos los préstamos en estado IN_STUDY (para el Analista Interno).
     */
    List<Loan> getPendingLoans(User analystUser);
}
