package app.domain.ports;

import app.domain.models.LogRecord;

import java.util.List;

/**
 * Puerto de salida — Repositorio de Bitácora de Operaciones.
 * Persiste en MongoDB (colección NoSQL de documentos).
 *
 * REGLA FUNDAMENTAL: Este repositorio es de SOLO ESCRITURA para operaciones normales.
 * Nunca se modifican ni eliminan registros — la bitácora es inmutable.
 * Las consultas son exclusivamente para auditoría (rol INTERNAL_ANALYST) y
 * para que los clientes vean el historial de sus propios productos.
 */
public interface ILogPort {

    /**
     * Inserta un nuevo registro en la bitácora.
     * Equivale a: mongoCollection.insertOne(document)
     * Es la única operación de escritura permitida.
     */
    void save(LogRecord record);

    /**
     * Retorna todos los registros de la bitácora.
     * Solo para: INTERNAL_ANALYST.
     */
    List<LogRecord> findAll();

    /**
     * Retorna los registros asociados a un producto específico
     * (número de cuenta, ID de préstamo, ID de transferencia).
     * Los clientes pueden ver los de sus propios productos.
     */
    List<LogRecord> findByAffectedProductId(String affectedProductId);

    /**
     * Retorna los registros generados por un usuario específico.
     * Solo para: INTERNAL_ANALYST.
     */
    List<LogRecord> findByUserId(int userId);

    /**
     * Retorna los registros de un tipo de operación específico.
     * Ejemplos: "LOAN_APPROVED", "TRANSFER_EXPIRED", "ACCOUNT_OPENED".
     * Solo para: INTERNAL_ANALYST.
     */
    List<LogRecord> findByOperationType(String operationType);
}