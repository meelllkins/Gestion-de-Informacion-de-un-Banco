package app.domain.enums;

public enum LoanStatus {
    PENDING, // Estado inicial al crear la solicitud
    APPROVED,
    REJECTED,
    DISBURSED // Desembolsado a la cuenta destino
}