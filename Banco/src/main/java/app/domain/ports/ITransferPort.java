package app.domain.ports;

import app.domain.enums.TransferStatus;
import app.domain.models.Transfer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida — Repositorio de Transferencias.
 * Persiste en MySQL.
 * Soporta transferencias individuales y pagos masivos (nómina).
 */
public interface ITransferPort {

    /** Guarda una nueva transferencia. */
    void save(Transfer transfer);

    /** Guarda un lote de transferencias (pagos masivos). */
    void saveAll(List<Transfer> transfers);

    /** Busca una transferencia por su ID. */
    Optional<Transfer> findById(int transferId);

    /** Retorna todas las transferencias en un estado específico. */
    List<Transfer> findByStatus(TransferStatus status);

    /**
     * Retorna transferencias donde la cuenta aparece como origen o destino.
     * Se usa para el historial de movimientos de una cuenta.
     */
    List<Transfer> findByAccount(String accountNumber);

    /**
     * Retorna transferencias en WAITING_APPROVAL cuya fecha de creación
     * sea anterior al momento indicado. Se usa para detectar vencimientos.
     */
    List<Transfer> findWaitingApprovalBefore(LocalDateTime dateTime);

    /**
     * Actualiza el estado de una transferencia.
     * También registra la fecha y el usuario aprobador si aplica.
     */
    void updateStatus(int transferId, TransferStatus newStatus,
                      LocalDateTime eventDate, Integer approverUserId);
}