package app.domain.services.implementations;

import app.domain.models.BankAccount;
import app.domain.models.User;
import app.domain.models.enums.AccountStatus;
import app.domain.models.enums.SystemRole;
import app.domain.models.enums.UserStatus;
import app.domain.ports.IAccountPort;
import app.domain.ports.IUserPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class OpenAccount {

    private final IAccountPort accountPort;
    private final IUserPort userPort;
    private final ValidateRole validateRole;
    private final LogOperation logOperation;

    @Autowired
    public OpenAccount(IAccountPort accountPort, IUserPort userPort,
                    ValidateRole validateRole, LogOperation logOperation) {
        this.accountPort = accountPort;
        this.userPort = userPort;
        this.validateRole = validateRole;
        this.logOperation = logOperation;
    }

    public BankAccount openAccount(BankAccount account, User requestingUser) {
        validateRole.validate(requestingUser,
                SystemRole.TELLER_EMPLOYEE, SystemRole.COMMERCIAL_EMPLOYEE);

        User holder = userPort.findByIdentificationId(account.getAccountHolderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un cliente con ese ID: " + account.getAccountHolderId()));

        if (holder.getUserStatus() == UserStatus.INACTIVE ||
            holder.getUserStatus() == UserStatus.BLOCKED) {
            throw new IllegalStateException(
                    "No se puede abrir una cuenta: el cliente está " + holder.getUserStatus());
        }

        if (account.getAccountNumber() == null || account.getAccountNumber().isBlank()) {
            throw new IllegalArgumentException("El número de cuenta es obligatorio.");
        }

        if (accountPort.existsByAccountNumber(account.getAccountNumber())) {
            throw new IllegalArgumentException(
                    "Ya existe una cuenta con el número: " + account.getAccountNumber());
        }

        if (account.getAccountType() == null) {
            throw new IllegalArgumentException("El tipo de cuenta es obligatorio.");
        }

        if (account.getBalance() < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo.");
        }

        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setOpeningDate(LocalDate.now().toString());
        accountPort.save(account);

        Map<String, Object> detail = new HashMap<>();
        detail.put("accountNumber", account.getAccountNumber());
        detail.put("accountType", account.getAccountType().toString());
        detail.put("holderId", account.getAccountHolderId());
        detail.put("initialBalance", account.getBalance());
        logOperation.log("ACCOUNT_OPENED", requestingUser, account.getAccountNumber(), detail);

        return account;
    }
}