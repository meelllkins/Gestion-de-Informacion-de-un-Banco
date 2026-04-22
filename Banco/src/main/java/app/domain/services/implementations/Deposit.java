package app.domain.services.implementations;

import app.domain.models.BankAccount;
import app.domain.models.User;
import app.domain.models.enums.SystemRole;
import app.domain.ports.IAccountPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Deposit {

    private final IAccountPort accountPort;
    private final ValidateRole validateRole;
    private final GetActiveAccount getActiveAccount;
    private final LogOperation logOperation;

    @Autowired
    public Deposit(IAccountPort accountPort, ValidateRole validateRole,
                   GetActiveAccount getActiveAccount, LogOperation logOperation) {
        this.accountPort = accountPort;
        this.validateRole = validateRole;
        this.getActiveAccount = getActiveAccount;
        this.logOperation = logOperation;
    }

    public void deposit(String accountNumber, double amount, User requestingUser) {
        validateRole.validate(requestingUser, SystemRole.TELLER_EMPLOYEE);

        if (amount <= 0) {
            throw new IllegalArgumentException("El monto del depósito debe ser mayor a cero.");
        }

        BankAccount account = getActiveAccount.getActiveAccount(accountNumber);
        double balanceBefore = account.getBalance();
        double newBalance = balanceBefore + amount;

        accountPort.updateBalance(accountNumber, newBalance);

        Map<String, Object> detail = new HashMap<>();
        detail.put("amount", amount);
        detail.put("balanceBefore", balanceBefore);
        detail.put("balanceAfter", newBalance);
        logOperation.log("DEPOSIT", requestingUser, accountNumber, detail);
    }
}