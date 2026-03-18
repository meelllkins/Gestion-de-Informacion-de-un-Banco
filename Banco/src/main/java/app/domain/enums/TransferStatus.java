package app.domain.enums;

public enum TransferStatus {
    PENDING,            // creada, sin ejecutar aún
    WAITING_APPROVAL,   // alto monto empresarial, esperando supervisor
    EXECUTED,           // fondos movidos
    APPROVED,           // aprobada por supervisor (antes de ejecutar)
    REJECTED,           // rechazada
    EXPIRED             // venció sin aprobación
}
