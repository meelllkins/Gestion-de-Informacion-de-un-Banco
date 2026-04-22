package app.domain.services.implementations;

import app.domain.models.BankAccount;
import app.domain.models.enums.AccountStatus;
import app.domain.ports.IAccountPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetActiveAccount {

    private final IAccountPort accountPort;

    @Autowired
    public GetActiveAccount(IAccountPort accountPort) {
        this.accountPort = accountPort;
    }

    public BankAccount getActiveAccount(String accountNumber) {
        BankAccount account = accountPort.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cuenta no encontrada: " + accountNumber));

        if (account.getAccountStatus() == AccountStatus.BLOCKED ||
            account.getAccountStatus() == AccountStatus.CANCELLED) {
            throw new IllegalStateException(
                    "La cuenta " + accountNumber + " está " +
                    account.getAccountStatus() + " y no puede realizar operaciones.");
        }

        return account;
    }
}