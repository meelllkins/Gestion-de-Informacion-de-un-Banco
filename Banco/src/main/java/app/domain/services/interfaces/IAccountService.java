package app.domain.services.interfaces;

import app.domain.models.BankAccount;
import app.domain.models.User;

import java.util.List;

/**
 * Servicio de gestión de cuentas bancarias.
 */
public interface IAccountService {

    /**
     * Abre una nueva cuenta bancaria para un cliente existente.
     * Reglas:
     * - El cliente no puede estar Inactivo o Bloqueado.
     * - El Numero_Cuenta debe ser único.
     * - El Tipo_Cuenta debe ser válido (del catálogo).
     * Solo pueden hacerlo: Empleado de Ventanilla, Empleado Comercial.
     */
    BankAccount openAccount(BankAccount account, User requestingUser);

    /**
     * Consulta una cuenta por su número.
     * La visibilidad depende del rol del usuario solicitante.
     */
    BankAccount findByAccountNumber(String accountNumber, User requestingUser);

    /**
     * Retorna todas las cuentas de un cliente específico.
     */
    List<BankAccount> getAccountsByHolder(String holderId, User requestingUser);

    /**
     * Realiza un depósito en una cuenta (operación de ventanilla).
     * Solo Empleado de Ventanilla puede hacerlo.
     */
    void deposit(String accountNumber, double amount, User requestingUser);

    /**
     * Realiza un retiro de una cuenta (operación de ventanilla).
     * Valida saldo suficiente y que la cuenta esté ACTIVE.
     * Solo Empleado de Ventanilla puede hacerlo.
     */
    void withdraw(String accountNumber, double amount, User requestingUser);
}
