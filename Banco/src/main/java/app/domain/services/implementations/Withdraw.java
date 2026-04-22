package app.domain.services.implementations;

import app.domain.enums.SystemRole;
import app.domain.models.BankAccount;
import app.domain.models.User;
import app.domain.ports.IAccountPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Withdraw {

    private final IAccountPort accountPort;
    private final ValidateRole validateRole;
    private final GetActiveAccount getActiveAccount;
    private final LogOperation logOperation;

    @Autowired
    public Withdraw(IAccountPort accountPort, ValidateRole validateRole,
                    GetActiveAccount getActiveAccount, LogOperation logOperation) {
        this.accountPort = accountPort;
        this.validateRole = validateRole;
        this.getActiveAccount = getActiveAccount;
        this.logOperation = logOperation;
    }

    public void withdraw(String accountNumber, double amount, User requestingUser) {
        validateRole.validate(requestingUser, SystemRole.TELLER_EMPLOYEE);

        if (amount <= 0) {
            throw new IllegalArgumentException("El monto del retiro debe ser mayor a cero.");
        }

        BankAccount account = getActiveAccount.getActiveAccount(accountNumber);

        if (account.getBalance() < amount) {
            throw new IllegalStateException(
                    "Saldo insuficiente. Disponible: " + account.getBalance() +
                    ", solicitado: " + amount);
        }

        double balanceBefore = account.getBalance();
        double newBalance = balanceBefore - amount;

        accountPort.updateBalance(accountNumber, newBalance);

        Map<String, Object> detail = new HashMap<>();
        detail.put("amount", amount);
        detail.put("balanceBefore", balanceBefore);
        detail.put("balanceAfter", newBalance);
        logOperation.log("WITHDRAWAL", requestingUser, accountNumber, detail);
    }
}