package app.domain.services.implementations;

import app.domain.models.BankAccount;
import app.domain.models.User;
import app.domain.ports.IAccountPort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Deposit {

    private final IAccountPort accountPort;
    private final GetActiveAccount getActiveAccount;
    private final LogOperation logOperation;

    public Deposit(IAccountPort accountPort,
                   GetActiveAccount getActiveAccount,
                   LogOperation logOperation) {
        this.accountPort = accountPort;
        this.getActiveAccount = getActiveAccount;
        this.logOperation = logOperation;
    }

    public void deposit(String accountNumber, double amount, User requestingUser) {
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