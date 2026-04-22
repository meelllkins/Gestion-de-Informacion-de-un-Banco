package app.domain.services.interfaces;

import app.domain.models.Transfer;
import app.domain.models.User;

import java.util.List;

/**
 * Servicio de gestión de transferencias.
 * Implementa el flujo: creación → (ejecución directa O espera de aprobación) → vencimiento.
 * Umbral de alto monto para empresas: definido como constante de negocio.
 */
public interface ITransferService {

    /**
     * Umbral de monto que requiere aprobación para transferencias empresariales.
     * Las transferencias que superen este valor quedan en WAITING_APPROVAL.
     */
    double HIGH_AMOUNT_THRESHOLD = 10_000_000.0; // Regla de negocio: ajustable

    /**
     * Crea una transferencia.
     * Reglas:
     * - Monto > 0.
     * - Cuenta origen ACTIVE y con saldo suficiente (si se ejecuta directamente).
     * - Si es Empleado de Empresa y monto > umbral → Estado: WAITING_APPROVAL.
     * - Si monto <= umbral → se ejecuta de inmediato (EXECUTED).
     * Pueden hacerlo: Cliente Persona Natural, Cliente Empresa, Empleado de Empresa.
     */
    Transfer createTransfer(Transfer transfer, User requestingUser);

    /**
     * Aprueba una transferencia empresarial de alto monto.
     * Transición: WAITING_APPROVAL → EXECUTED.
     * Valida saldo en Cuenta_Origen antes de ejecutar.
     * Solo puede hacerlo: Supervisor de Empresa.
     */
    Transfer approveTransfer(int transferId, User supervisorUser);

    /**
     * Rechaza una transferencia empresarial.
     * Transición: WAITING_APPROVAL → REJECTED.
     * Solo puede hacerlo: Supervisor de Empresa.
     */
    Transfer rejectTransfer(int transferId, User supervisorUser);

    /**
     * Revisa y marca como EXPIRED las transferencias en WAITING_APPROVAL
     * que lleven más de 60 minutos sin ser aprobadas.
     * Este método debe ser llamado periódicamente (scheduler o al consultar).
     * Registra el vencimiento en la Bitácora.
     */
    void checkAndExpireTransfers();

    /**
     * Consulta una transferencia por su ID.
     */
    Transfer findById(int transferId, User requestingUser);

    /**
     * Retorna todas las transferencias en WAITING_APPROVAL de una empresa.
     * Solo para Supervisor de Empresa o Analista Interno.
     */
    List<Transfer> getPendingApprovalTransfers(User requestingUser);

    /**
     * Retorna el historial de transferencias de un cliente o empresa.
     */
    List<Transfer> getTransferHistory(String accountNumber, User requestingUser);
}
