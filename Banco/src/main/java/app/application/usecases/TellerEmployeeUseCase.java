package app.application.usecases;

import app.application.RegisterTellerEmployee;
import app.domain.models.BankAccount;
import app.domain.models.User;
import app.domain.ports.IUserPort;
import app.domain.services.implementations.Deposit;
import app.domain.services.implementations.GetAccountsByHolder;
import app.domain.services.implementations.OpenAccount;
import app.domain.services.implementations.Withdraw;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TellerEmployeeUseCase {

    private final RegisterTellerEmployee registerTellerEmployee;
    private final OpenAccount openAccount;
    private final GetAccountsByHolder accountsByHolderService;
    private final IUserPort userPort;
    private final Deposit deposit;
    private final Withdraw withdraw;

    public TellerEmployeeUseCase(RegisterTellerEmployee registerTellerEmployee,
                                  OpenAccount openAccount,
                                  GetAccountsByHolder accountsByHolderService,
                                  IUserPort userPort,
                                  Deposit deposit,
                                  Withdraw withdraw) {
        this.registerTellerEmployee = registerTellerEmployee;
        this.openAccount = openAccount;
        this.accountsByHolderService = accountsByHolderService;
        this.userPort = userPort;
        this.deposit = deposit;
        this.withdraw = withdraw;
    }

    public User register(User employee, String username, String password) {
        return registerTellerEmployee.register(employee, username, password);
    }

    public BankAccount openAccount(BankAccount account, String tellerIdentificationId) {
        User teller = userPort.findByIdentificationId(tellerIdentificationId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
        return openAccount.openAccount(account, teller);
    }

    public List<BankAccount> getAccountsByHolder(String customerIdentificationId,
                                                  String tellerIdentificationId) {
        User teller = userPort.findByIdentificationId(tellerIdentificationId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
        return accountsByHolderService.getAccountsByHolder(customerIdentificationId, teller);
    }

    public void deposit(String accountNumber, double amount, String tellerIdentificationId) {
        User teller = userPort.findByIdentificationId(tellerIdentificationId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
        deposit.deposit(accountNumber, amount, teller);
    }

    public void withdraw(String accountNumber, double amount, String tellerIdentificationId) {
        User teller = userPort.findByIdentificationId(tellerIdentificationId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
        withdraw.withdraw(accountNumber, amount, teller);
    }
}
