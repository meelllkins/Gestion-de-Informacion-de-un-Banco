package app.domain.ports;

import app.domain.models.BankAccount;
import app.domain.models.enums.AccountStatus;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida — Repositorio de Cuentas Bancarias.
 * Persiste en MySQL. El saldo NUNCA se modifica directamente desde aquí;
 * siempre pasa por un flujo de negocio (depósito, retiro, transferencia, desembolso).
 */
public interface IAccountPort {

    /** Guarda una nueva cuenta bancaria. */
    void save(BankAccount account);

    /** Busca una cuenta por su número único. */
    Optional<BankAccount> findByAccountNumber(String accountNumber);

    /** Retorna todas las cuentas de un titular (por su ID de identificación). */
    List<BankAccount> findByHolderId(String holderId);

    /** Actualiza el saldo de una cuenta. Solo llamar desde flujos de negocio validados. */
    void updateBalance(String accountNumber, double newBalance);

    /** Actualiza el estado de una cuenta (ACTIVE, BLOCKED, CANCELLED). */
    void updateStatus(String accountNumber, AccountStatus newStatus);

    /** Verifica si ya existe una cuenta con ese número. */
    boolean existsByAccountNumber(String accountNumber);
}