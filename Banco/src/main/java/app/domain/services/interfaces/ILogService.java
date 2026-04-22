package app.domain.services.interfaces;

import app.domain.models.LogRecord;
import app.domain.models.User;

import java.util.List;
import java.util.Map;

/**
 * Servicio de Bitácora de Operaciones (NoSQL).
 * Registra todas las operaciones significativas para auditoría.
 * La bitácora es de solo escritura para operaciones normales — inmutable.
 */
public interface ILogService {

    /**
     * Registra una operación en la bitácora.
     * @param operationType  Tipo de operación (ej: "TRANSFER_EXECUTED", "LOAN_APPROVED")
     * @param user           Usuario que ejecutó la acción
     * @param affectedProductId  ID del producto afectado (cuenta, préstamo, transferencia)
     * @param detailData     Mapa con datos específicos según el tipo de operación
     */
    void log(String operationType, User user, String affectedProductId, Map<String, Object> detailData);

    /**
     * Retorna el historial completo de la bitácora.
     * Solo para: Analista Interno del Banco.
     */
    List<LogRecord> getAllLogs(User requestingUser);

    /**
     * Retorna los registros de bitácora asociados a un producto específico.
     * Clientes solo pueden ver los de sus propios productos.
     */
    List<LogRecord> getLogsByProduct(String affectedProductId, User requestingUser);

    /**
     * Retorna los registros de bitácora de un usuario específico.
     * Solo para: Analista Interno del Banco.
     */
    List<LogRecord> getLogsByUser(String userId, User requestingUser);
}
