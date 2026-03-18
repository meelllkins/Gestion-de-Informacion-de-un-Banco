package app.domain.services.implementations;

import app.domain.enums.SystemRole;
import app.domain.enums.TransferStatus;
import app.domain.models.BankAccount;
import app.domain.models.Transfer;
import app.domain.models.User;
import app.domain.services.interfaces.IAuthService;
import app.domain.services.interfaces.ILogService;
import app.domain.services.interfaces.ITransferService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class TransferService implements ITransferService {

    private final List<Transfer> transfers = new ArrayList<>();
    private final IAuthService authService;
    private final ILogService logService;
    private final AccountService accountService; // Para validar y modificar saldos

    private int nextTransferId = 1;

    public TransferService(IAuthService authService, ILogService logService, AccountService accountService) {
        this.authService = authService;
        this.logService = logService;
        this.accountService = accountService;
    }

    @Override
    public Transfer createTransfer(Transfer transfer, User requestingUser) {
        // Roles que pueden crear transferencias
        authService.validateRole(requestingUser,
                SystemRole.INDIVIDUAL_CUSTOMER,
                SystemRole.CORPORATE_CUSTOMER,
                SystemRole.CORPORATE_EMPLOYEE);

        // Regla: monto > 0
        if (transfer.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto de la transferencia debe ser mayor a cero.");
        }

        // Validar cuenta origen activa
        BankAccount sourceAccount = accountService.getActiveAccount(transfer.getSourceAccount());

        // Restricción: Empleado de Empresa solo opera cuentas de su empresa
        if (requestingUser.getSystemRole() == SystemRole.CORPORATE_EMPLOYEE) {
            if (!sourceAccount.getAccountHolderId().equals(requestingUser.getRelatedid())) {
                throw new SecurityException("Solo puede operar cuentas de su empresa.");
            }
        }

        // Restricción: Cliente Persona Natural / Empresa solo opera sus propias cuentas
        if (requestingUser.getSystemRole() == SystemRole.INDIVIDUAL_CUSTOMER ||
            requestingUser.getSystemRole() == SystemRole.CORPORATE_CUSTOMER) {
            if (!sourceAccount.getAccountHolderId().equals(requestingUser.getIdentificationId())) {
                throw new SecurityException("Solo puede transferir desde sus propias cuentas.");
            }
        }

        transfer.setTransferId(nextTransferId++);
        transfer.setCreationDate(LocalDateTime.now());
        transfer.setCreatorUserId(Integer.parseInt(requestingUser.getIdentificationId()));

        // Determinar si requiere aprobación (Empleado de Empresa + alto monto)
        boolean requiresApproval = requestingUser.getSystemRole() == SystemRole.CORPORATE_EMPLOYEE
                && transfer.getAmount() > HIGH_AMOUNT_THRESHOLD;

        if (requiresApproval) {
            // Flujo de aprobación: queda en espera
            transfer.setTransferStatus(TransferStatus.PENDING);
            transfers.add(transfer);

            Map<String, Object> detail = new HashMap<>();
            detail.put("amount", transfer.getAmount());
            detail.put("sourceAccount", transfer.getSourceAccount());
            detail.put("destinationAccount", transfer.getDestinationAccount());
            detail.put("reason", "Supera el umbral de alto monto: " + HIGH_AMOUNT_THRESHOLD);
            logService.log("TRANSFER_PENDING", requestingUser,
                    String.valueOf(transfer.getTransferId()), detail);

            System.out.printf("Transferencia #%d creada. Monto %.2f supera umbral → PENDING%n",
                    transfer.getTransferId(), transfer.getAmount());
        } else {
            // Ejecución directa: validar saldo y ejecutar
            executeTransfer(transfer, sourceAccount, requestingUser);
        }

        return transfer;
    }

    @Override
    public Transfer approveTransfer(int transferId, User supervisorUser) {
        authService.validateRole(supervisorUser, SystemRole.CORPORATE_SUPERVISOR);

        Transfer transfer = getTransferById(transferId);

        // Verificar que esté en el estado correcto
        if (transfer.getTransferStatus() != TransferStatus.PENDING) {
            throw new IllegalStateException(
                "Solo se pueden aprobar transferencias en estado PENDING. Estado actual: "
                + transfer.getTransferStatus());
        }

        // Verificar que el supervisor sea de la misma empresa
        BankAccount sourceAccount = accountService.getActiveAccount(transfer.getSourceAccount());
        if (!sourceAccount.getAccountHolderId().equals(supervisorUser.getRelatedid())) {
            throw new SecurityException("Solo puede aprobar transferencias de su empresa.");
        }

        // Verificar que no haya vencido (ejecutar el chequeo de vencimiento primero)
        checkAndExpireTransfers();
        // Re-obtener por si ya venció
        transfer = getTransferById(transferId);
        if (transfer.getTransferStatus() == TransferStatus.EXPIRED) {
            throw new IllegalStateException("La transferencia #" + transferId + " ya venció.");
        }

        // Ejecutar la transferencia
        executeTransfer(transfer, sourceAccount, supervisorUser);
        transfer.setApprovalDate(LocalDateTime.now());
        transfer.setApproverUserId(Integer.parseInt(supervisorUser.getIdentificationId()));

        System.out.println("Transferencia #" + transferId + " APROBADA y EJECUTADA.");
        return transfer;
    }

    @Override
    public Transfer rejectTransfer(int transferId, User supervisorUser) {
        authService.validateRole(supervisorUser, SystemRole.CORPORATE_SUPERVISOR);

        Transfer transfer = getTransferById(transferId);

        if (transfer.getTransferStatus() != TransferStatus.PENDING) {
            throw new IllegalStateException(
                "Solo se pueden rechazar transferencias en estado PENDING.");
        }

        transfer.setTransferStatus(TransferStatus.REJECTED);
        transfer.setApprovalDate(LocalDateTime.now());
        transfer.setApproverUserId(Integer.parseInt(supervisorUser.getIdentificationId()));

        Map<String, Object> detail = new HashMap<>();
        detail.put("amount", transfer.getAmount());
        detail.put("sourceAccount", transfer.getSourceAccount());
        detail.put("supervisorId", supervisorUser.getIdentificationId());
        logService.log("TRANSFER_REJECTED", supervisorUser, String.valueOf(transferId), detail);

        System.out.println("Transferencia #" + transferId + " RECHAZADA.");
        return transfer;
    }

    @Override
    public void checkAndExpireTransfers() {
        LocalDateTime now = LocalDateTime.now();

        transfers.stream()
                .filter(t -> t.getTransferStatus() == TransferStatus.PENDING)
                .filter(t -> ChronoUnit.MINUTES.between(t.getCreationDate(), now) >= 60)
                .forEach(t -> {
                    t.setTransferStatus(TransferStatus.EXPIRED);

                    // Crear un usuario sistema para registrar el vencimiento automático
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("reason", "Vencida por falta de aprobación en el tiempo establecido");
                    detail.put("expirationDateTime", now.toString());
                    detail.put("creatorUserId", t.getCreatorUserId());
                    detail.put("amount", t.getAmount());
                    detail.put("sourceAccount", t.getSourceAccount());

                    // Registrar en bitácora con usuario sistema (id=0 como proceso automático)
                    User systemUser = new User();
                    systemUser.setIdentificationId("0");
                    systemUser.setSystemRole(SystemRole.INTERNAL_ANALYST);
                    systemUser.setName("SISTEMA");

                    logService.log("TRANSFER_EXPIRED", systemUser, String.valueOf(t.getTransferId()), detail);
                    System.out.println("Transferencia #" + t.getTransferId() + " VENCIDA automáticamente.");
                });
    }

    @Override
    public Transfer findById(int transferId, User requestingUser) {
        checkAndExpireTransfers(); // Siempre verificar vencimientos al consultar
        Transfer transfer = getTransferById(transferId);

        // Restricciones de visibilidad
        validateTransferVisibility(transfer, requestingUser);
        return transfer;
    }

    @Override
    public List<Transfer> getPendingApprovalTransfers(User requestingUser) {
        authService.validateRole(requestingUser,
                SystemRole.CORPORATE_SUPERVISOR, SystemRole.INTERNAL_ANALYST);

        checkAndExpireTransfers();

        return transfers.stream()
                .filter(t -> t.getTransferStatus() == TransferStatus.PENDING)
                .filter(t -> {
                    // Supervisor de empresa solo ve las de su empresa
                    if (requestingUser.getSystemRole() == SystemRole.CORPORATE_SUPERVISOR) {
                        try {
                            BankAccount src = accountService.getActiveAccount(t.getSourceAccount());
                            return src.getAccountHolderId().equals(requestingUser.getRelatedid());
                        } catch (Exception e) {
                            return false;
                        }
                    }
                    return true; // Analista ve todas
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Transfer> getTransferHistory(String accountNumber, User requestingUser) {
        checkAndExpireTransfers();

        // Clientes solo ven su historial
        if (requestingUser.getSystemRole() == SystemRole.INDIVIDUAL_CUSTOMER ||
            requestingUser.getSystemRole() == SystemRole.CORPORATE_CUSTOMER) {
            BankAccount account = accountService.getActiveAccount(accountNumber);
            if (!account.getAccountHolderId().equals(requestingUser.getIdentificationId())) {
                throw new SecurityException("No puede ver el historial de cuentas de otro cliente.");
            }
        }

        return transfers.stream()
                .filter(t -> t.getSourceAccount().equals(accountNumber)
                          || t.getDestinationAccount().equals(accountNumber))
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    // Métodos privados
    // ──────────────────────────────────────────────

    /**
     * Lógica central de ejecución de una transferencia.
     * Descuenta saldo de origen y abona a destino.
     */
    private void executeTransfer(Transfer transfer, BankAccount sourceAccount, User executingUser) {
        // Validar saldo suficiente
        if (sourceAccount.getBalance() < transfer.getAmount()) {
            throw new IllegalStateException(
                "Saldo insuficiente en cuenta origen. Disponible: " + sourceAccount.getBalance()
                + ", solicitado: " + transfer.getAmount());
        }

        double srcBefore = sourceAccount.getBalance();
        sourceAccount.setBalance(sourceAccount.getBalance() - transfer.getAmount());

        // Intentar abonar a la cuenta destino (si es interna)
        double dstBefore = 0;
        double dstAfter = 0;
        try {
            BankAccount destAccount = accountService.getActiveAccount(transfer.getDestinationAccount());
            dstBefore = destAccount.getBalance();
            destAccount.setBalance(destAccount.getBalance() + transfer.getAmount());
            dstAfter = destAccount.getBalance();
        } catch (IllegalArgumentException e) {
            // Cuenta destino externa o no encontrada: solo se descuenta el origen
            System.out.println("Cuenta destino no encontrada en el sistema (puede ser externa): "
                    + transfer.getDestinationAccount());
        }

        transfer.setTransferStatus(TransferStatus.EXECUTED);
        transfers.add(transfer);

        Map<String, Object> detail = new HashMap<>();
        detail.put("amount", transfer.getAmount());
        detail.put("sourceAccount", transfer.getSourceAccount());
        detail.put("destinationAccount", transfer.getDestinationAccount());
        detail.put("balanceBeforeSource", srcBefore);
        detail.put("balanceAfterSource", sourceAccount.getBalance());
        detail.put("balanceBeforeDestination", dstBefore);
        detail.put("balanceAfterDestination", dstAfter);
        logService.log("TRANSFER_EXECUTED", executingUser, String.valueOf(transfer.getTransferId()), detail);

        System.out.printf("Transferencia #%d EJECUTADA. %.2f de %s → %s%n",
                transfer.getTransferId(), transfer.getAmount(),
                transfer.getSourceAccount(), transfer.getDestinationAccount());
    }

    private Transfer getTransferById(int transferId) {
        return transfers.stream()
                .filter(t -> t.getTransferId() == transferId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Transferencia no encontrada: #" + transferId));
    }

    private void validateTransferVisibility(Transfer transfer, User requestingUser) {
        SystemRole role = requestingUser.getSystemRole();
        if (role == SystemRole.INDIVIDUAL_CUSTOMER || role == SystemRole.CORPORATE_CUSTOMER) {
            try {
                BankAccount src = accountService.getActiveAccount(transfer.getSourceAccount());
                if (!src.getAccountHolderId().equals(requestingUser.getIdentificationId())) {
                    throw new SecurityException("No tiene permiso para ver esta transferencia.");
                }
            } catch (IllegalArgumentException e) {
                throw new SecurityException("No tiene permiso para ver esta transferencia.");
            }
        }
    }
}
