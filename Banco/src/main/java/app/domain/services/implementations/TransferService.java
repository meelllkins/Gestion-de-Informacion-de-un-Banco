package app.domain.services.implementations;

import app.domain.models.BankAccount;
import app.domain.models.Transfer;
import app.domain.models.User;
import app.domain.models.enums.SystemRole;
import app.domain.models.enums.TransferStatus;
import app.domain.ports.IAccountPort;
import app.domain.services.interfaces.ILogService;
import app.domain.services.interfaces.ITransferService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransferService implements ITransferService {

    private final List<Transfer> transfers = new ArrayList<>();
    private final ILogService logService;
    private final GetActiveAccount getActiveAccount;
    private final IAccountPort accountPort;

    private int nextTransferId = 1;

    public TransferService(ILogService logService,
                           GetActiveAccount getActiveAccount,
                           IAccountPort accountPort) {
        this.logService = logService;
        this.getActiveAccount = getActiveAccount;
        this.accountPort = accountPort;
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
            executeTransfer(transfer, sourceAccount, requestingUser);
        }

        return transfer;
    }

    @Override
    public Transfer approveTransfer(int transferId, User supervisorUser) {
        Transfer transfer = getTransferById(transferId);

        if (transfer.getTransferStatus() != TransferStatus.PENDING) {
            throw new IllegalStateException(
                "Solo se pueden aprobar transferencias en estado PENDING. Estado actual: "
                + transfer.getTransferStatus());
        }

        BankAccount sourceAccount = getActiveAccount.getActiveAccount(transfer.getSourceAccount());
        if (!sourceAccount.getAccountHolderId().equals(supervisorUser.getRelatedId())) {
            throw new SecurityException("Solo puede aprobar transferencias de su empresa.");
        }

        checkAndExpireTransfers();
        transfer = getTransferById(transferId);
        if (transfer.getTransferStatus() == TransferStatus.EXPIRED) {
            throw new IllegalStateException("La transferencia #" + transferId + " ya venció.");
        }

        executeTransfer(transfer, sourceAccount, supervisorUser);
        transfer.setApprovalDate(LocalDateTime.now());
        transfer.setApproverUserId(Integer.parseInt(supervisorUser.getIdentificationId()));

        System.out.println("Transferencia #" + transferId + " APROBADA y EJECUTADA.");
        return transfer;
    }

    @Override
    public Transfer rejectTransfer(int transferId, User supervisorUser) {
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

                    Map<String, Object> detail = new HashMap<>();
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
        Transfer transfer = getTransferById(transferId);
        validateTransferVisibility(transfer, requestingUser);
        return transfer;
    }

    @Override
    public List<Transfer> getPendingApprovalTransfers(User requestingUser) {
        checkAndExpireTransfers();

        return transfers.stream()
                .filter(t -> t.getTransferStatus() == TransferStatus.PENDING)
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

        return transfers.stream()
                .filter(t -> t.getSourceAccount().equals(accountNumber)
                          || t.getDestinationAccount().equals(accountNumber))
                .collect(Collectors.toList());
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

        transfer.setTransferStatus(TransferStatus.EXECUTED);
        transfers.add(transfer);

        Map<String, Object> detail = new HashMap<>();
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

    private Transfer getTransferById(int transferId) {
        return transfers.stream()
                .filter(t -> t.getTransferId() == transferId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transferencia no encontrada: #" + transferId));
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