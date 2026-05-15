package app.domain.services.implementations;

import app.domain.models.BankAccount;
import app.domain.models.Transfer;
import app.domain.models.User;
import app.domain.models.enums.SystemRole;
import app.domain.models.enums.TransferStatus;
import app.domain.Exceptions.BusinessException;
import app.domain.ports.IAccountPort;
import app.domain.ports.ITransferPort;
import app.domain.services.interfaces.ILogService;
import app.domain.services.interfaces.ITransferService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransferService implements ITransferService {

    private final ILogService logService;
    private final GetActiveAccount getActiveAccount;
    private final IAccountPort accountPort;
    private final ITransferPort transferPort;

    private int nextTransferId = 1;

    public TransferService(ILogService logService,
                           GetActiveAccount getActiveAccount,
                           IAccountPort accountPort,
                           ITransferPort transferPort) {
        this.logService = logService;
        this.getActiveAccount = getActiveAccount;
        this.accountPort = accountPort;
        this.transferPort = transferPort;
    }

    @Override
    public Transfer createTransfer(Transfer transfer, User requestingUser) {
        if (transfer.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto de la transferencia debe ser mayor a cero.");
        }

        BankAccount sourceAccount = getActiveAccount.getActiveAccount(transfer.getSourceAccount());

        if (requestingUser.getSystemRole() == SystemRole.CORPORATE_EMPLOYEE) {
            if (!sourceAccount.getAccountHolderId().equals(requestingUser.getRelatedId())) {
                throw new SecurityException("Solo puede operar cuentas de su empresa.");
            }
        }

        if (requestingUser.getSystemRole() == SystemRole.INDIVIDUAL_CUSTOMER ||
            requestingUser.getSystemRole() == SystemRole.CORPORATE_CUSTOMER) {
            if (!sourceAccount.getAccountHolderId().equals(requestingUser.getIdentificationId())) {
                throw new SecurityException("Solo puede transferir desde sus propias cuentas.");
            }
        }

        transfer.setTransferId(nextTransferId++);
        transfer.setCreationDate(LocalDateTime.now());
        transfer.setCreatorUserId(Integer.parseInt(requestingUser.getIdentificationId()));

        boolean requiresApproval = requestingUser.getSystemRole() == SystemRole.CORPORATE_EMPLOYEE
                && transfer.getAmount() > HIGH_AMOUNT_THRESHOLD;

        if (requiresApproval) {
            transfer.setTransferStatus(TransferStatus.WAITING_APPROVAL);
            transferPort.save(transfer);

            Map<String, Object> detail = new HashMap<>();
            detail.put("amount", transfer.getAmount());
            detail.put("sourceAccount", transfer.getSourceAccount());
            detail.put("destinationAccount", transfer.getDestinationAccount());
            detail.put("reason", "Supera el umbral de alto monto: " + HIGH_AMOUNT_THRESHOLD);
            logService.log("TRANSFER_PENDING", requestingUser,
                    String.valueOf(transfer.getTransferId()), detail);

            System.out.printf("Transferencia #%d creada. Monto %.2f supera umbral → WAITING_APPROVAL%n",
                    transfer.getTransferId(), transfer.getAmount());
        } else {
            executeTransfer(transfer, sourceAccount, requestingUser);
            transferPort.save(transfer);
        }

        return transfer;
    }

    @Override
    public Transfer approveTransfer(int transferId, User supervisorUser) {
        Transfer transfer = transferPort.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transferencia no encontrada: #" + transferId));

        if (transfer.getTransferStatus() != TransferStatus.WAITING_APPROVAL) {
            throw new IllegalStateException(
                "Solo se pueden aprobar transferencias en estado WAITING_APPROVAL. Estado actual: "
                + transfer.getTransferStatus());
        }

        BankAccount sourceAccount = getActiveAccount.getActiveAccount(transfer.getSourceAccount());
        if (!sourceAccount.getAccountHolderId().equals(supervisorUser.getRelatedId())) {
            throw new SecurityException("Solo puede aprobar transferencias de su empresa.");
        }

        if (transfer.getCreatorUserId() == Integer.parseInt(supervisorUser.getIdentificationId())) {
            throw new BusinessException(
                    "Un supervisor no puede aprobar transferencias que él mismo creó.");
        }

        checkAndExpireTransfers();
        transfer = transferPort.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transferencia no encontrada: #" + transferId));
        if (transfer.getTransferStatus() == TransferStatus.EXPIRED) {
            throw new IllegalStateException("La transferencia #" + transferId + " ya venció.");
        }

        executeTransfer(transfer, sourceAccount, supervisorUser);
        LocalDateTime approvalDate = LocalDateTime.now();
        transfer.setApprovalDate(approvalDate);
        transfer.setApproverUserId(Integer.parseInt(supervisorUser.getIdentificationId()));
        transferPort.updateStatus(transferId, TransferStatus.EXECUTED,
                approvalDate, transfer.getApproverUserId());

        System.out.println("Transferencia #" + transferId + " APROBADA y EJECUTADA.");
        return transfer;
    }

    @Override
    public Transfer rejectTransfer(int transferId, User supervisorUser) {
        Transfer transfer = transferPort.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transferencia no encontrada: #" + transferId));

        if (transfer.getTransferStatus() != TransferStatus.WAITING_APPROVAL) {
            throw new IllegalStateException(
                "Solo se pueden rechazar transferencias en estado WAITING_APPROVAL.");
        }

        transfer.setTransferStatus(TransferStatus.REJECTED);
        LocalDateTime rejectionDate = LocalDateTime.now();
        transfer.setApprovalDate(rejectionDate);
        transfer.setApproverUserId(Integer.parseInt(supervisorUser.getIdentificationId()));
        transferPort.updateStatus(transferId, TransferStatus.REJECTED,
                rejectionDate, transfer.getApproverUserId());

        Map<String, Object> detail = new HashMap<>();
        detail.put("previousStatus", TransferStatus.WAITING_APPROVAL.toString());
        detail.put("newStatus", TransferStatus.REJECTED.toString());
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

        transferPort.findWaitingApprovalBefore(now.minusMinutes(60))
                .forEach(t -> {
                    t.setTransferStatus(TransferStatus.EXPIRED);
                    transferPort.updateStatus(t.getTransferId(), TransferStatus.EXPIRED, now, null);

                    Map<String, Object> detail = new HashMap<>();
                    detail.put("previousStatus", TransferStatus.WAITING_APPROVAL.toString());
                    detail.put("newStatus", TransferStatus.EXPIRED.toString());
                    detail.put("reason", "Vencida por falta de aprobación en el tiempo establecido");
                    detail.put("expirationDateTime", now.toString());
                    detail.put("creatorUserId", t.getCreatorUserId());
                    detail.put("amount", t.getAmount());
                    detail.put("sourceAccount", t.getSourceAccount());

                    User systemUser = new User();
                    systemUser.setIdentificationId("0");
                    systemUser.setSystemRole(SystemRole.INTERNAL_ANALYST);
                    systemUser.setName("SISTEMA");

                    logService.log("TRANSFER_EXPIRED", systemUser,
                            String.valueOf(t.getTransferId()), detail);
                    System.out.println("Transferencia #" + t.getTransferId() + " VENCIDA automáticamente.");
                });
    }

    @Override
    public Transfer findById(int transferId, User requestingUser) {
        checkAndExpireTransfers();
        Transfer transfer = transferPort.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transferencia no encontrada: #" + transferId));
        validateTransferVisibility(transfer, requestingUser);
        return transfer;
    }

    @Override
    public List<Transfer> getPendingApprovalTransfers(User requestingUser) {
        checkAndExpireTransfers();

        return transferPort.findByStatus(TransferStatus.WAITING_APPROVAL).stream()
                .filter(t -> {
                    if (requestingUser.getSystemRole() == SystemRole.CORPORATE_SUPERVISOR) {
                        try {
                            BankAccount src = getActiveAccount.getActiveAccount(t.getSourceAccount());
                            return src.getAccountHolderId().equals(requestingUser.getRelatedId());
                        } catch (Exception e) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Transfer> getTransferHistory(String accountNumber, User requestingUser) {
        checkAndExpireTransfers();

        if (requestingUser.getSystemRole() == SystemRole.INDIVIDUAL_CUSTOMER ||
            requestingUser.getSystemRole() == SystemRole.CORPORATE_CUSTOMER) {
            BankAccount account = getActiveAccount.getActiveAccount(accountNumber);
            if (!account.getAccountHolderId().equals(requestingUser.getIdentificationId())) {
                throw new SecurityException("No puede ver el historial de cuentas de otro cliente.");
            }
        }

        return transferPort.findByAccount(accountNumber);
    }

    @Override
    public List<Transfer> createBulkTransfer(List<Transfer> transfers, User requestingUser) {
        if (transfers == null || transfers.isEmpty()) {
            throw new IllegalArgumentException("La lista de transferencias no puede estar vacía.");
        }

        LocalDateTime now = LocalDateTime.now();
        int creatorId = Integer.parseInt(requestingUser.getIdentificationId());

        // PRE-FLIGHT: valida montos, pertenencia de cuentas y saldo acumulado
        // sin tocar la base de datos hasta que todo sea válido
        Map<String, Double> runningBalance = new HashMap<>();
        for (Transfer t : transfers) {
            if (t.getAmount() <= 0) {
                throw new IllegalArgumentException(
                        "Todos los montos deben ser mayores a cero.");
            }
            String src = t.getSourceAccount();
            if (!runningBalance.containsKey(src)) {
                BankAccount account = getActiveAccount.getActiveAccount(src);
                if (!account.getAccountHolderId().equals(requestingUser.getRelatedId())) {
                    throw new SecurityException(
                            "Solo puede operar cuentas de su empresa: " + src);
                }
                runningBalance.put(src, account.getBalance());
            }
            double available = runningBalance.get(src);
            if (available < t.getAmount()) {
                throw new BusinessException(
                        "Saldo insuficiente en " + src + " para cubrir la nómina completa. " +
                        "Disponible acumulado: " + available +
                        ", solicitado en este pago: " + t.getAmount());
            }
            runningBalance.put(src, available - t.getAmount());
        }

        // EJECUCIÓN: todas las validaciones pasaron; ejecuta y persiste en lote
        List<Transfer> processed = new ArrayList<>();
        for (Transfer t : transfers) {
            t.setTransferId(nextTransferId++);
            t.setCreationDate(now);
            t.setCreatorUserId(creatorId);
            BankAccount freshSource = getActiveAccount.getActiveAccount(t.getSourceAccount());
            executeTransfer(t, freshSource, requestingUser);
            processed.add(t);
        }

        transferPort.saveAll(processed);
        return processed;
    }

    // ──────────────────────────────────────────────
    // Métodos privados
    // ──────────────────────────────────────────────

    private void executeTransfer(Transfer transfer, BankAccount sourceAccount, User executingUser) {
        if (sourceAccount.getBalance() < transfer.getAmount()) {
            throw new IllegalStateException(
                "Saldo insuficiente en cuenta origen. Disponible: " + sourceAccount.getBalance()
                + ", solicitado: " + transfer.getAmount());
        }

        double srcBefore = sourceAccount.getBalance();
        double srcAfter = srcBefore - transfer.getAmount();
        accountPort.updateBalance(transfer.getSourceAccount(), srcAfter);

        double dstBefore = 0;
        double dstAfter = 0;
        try {
            BankAccount destAccount = getActiveAccount.getActiveAccount(transfer.getDestinationAccount());
            dstBefore = destAccount.getBalance();
            dstAfter = dstBefore + transfer.getAmount();
            accountPort.updateBalance(transfer.getDestinationAccount(), dstAfter);
        } catch (IllegalArgumentException e) {
            System.out.println("Cuenta destino no encontrada en el sistema (puede ser externa): "
                    + transfer.getDestinationAccount());
        }

        TransferStatus previousStatus = transfer.getTransferStatus();
        transfer.setTransferStatus(TransferStatus.EXECUTED);

        Map<String, Object> detail = new HashMap<>();
        detail.put("previousStatus", previousStatus != null ? previousStatus.toString() : "NEW");
        detail.put("newStatus", TransferStatus.EXECUTED.toString());
        detail.put("amount", transfer.getAmount());
        detail.put("sourceAccount", transfer.getSourceAccount());
        detail.put("destinationAccount", transfer.getDestinationAccount());
        detail.put("balanceBeforeSource", srcBefore);
        detail.put("balanceAfterSource", srcAfter);
        detail.put("balanceBeforeDestination", dstBefore);
        detail.put("balanceAfterDestination", dstAfter);
        logService.log("TRANSFER_EXECUTED", executingUser,
                String.valueOf(transfer.getTransferId()), detail);

        System.out.printf("Transferencia #%d EJECUTADA. %.2f de %s → %s%n",
                transfer.getTransferId(), transfer.getAmount(),
                transfer.getSourceAccount(), transfer.getDestinationAccount());
    }

    private void validateTransferVisibility(Transfer transfer, User requestingUser) {
        SystemRole role = requestingUser.getSystemRole();
        if (role == SystemRole.INDIVIDUAL_CUSTOMER || role == SystemRole.CORPORATE_CUSTOMER) {
            try {
                BankAccount src = getActiveAccount.getActiveAccount(transfer.getSourceAccount());
                if (!src.getAccountHolderId().equals(requestingUser.getIdentificationId())) {
                    throw new SecurityException("No tiene permiso para ver esta transferencia.");
                }
            } catch (IllegalArgumentException e) {
                throw new SecurityException("No tiene permiso para ver esta transferencia.");
            }
        }
    }
}
