package app.domain.services.implementations;

import app.domain.models.BankAccount;
import app.domain.models.User;
import app.domain.models.enums.SystemRole;
import app.domain.ports.IAccountPort;
import org.springframework.stereotype.Service;

@Service
public class FindAccountByNumber {

    private final IAccountPort accountPort;


    public FindAccountByNumber(IAccountPort accountPort) {
        this.accountPort = accountPort;
    }

    public BankAccount findByAccountNumber(String accountNumber, User requestingUser) {
        BankAccount account = accountPort.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la cuenta: " + accountNumber));

        checkVisibility(account, requestingUser);
        return account;
    }

    private void checkVisibility(BankAccount account, User requestingUser) {
        SystemRole role = requestingUser.getSystemRole();
        if (role == SystemRole.INDIVIDUAL_CUSTOMER || role == SystemRole.CORPORATE_CUSTOMER) {
            if (!account.getAccountHolderId().equals(requestingUser.getIdentificationId())) {
                throw new SecurityException("No tiene permiso para ver esta cuenta.");
            }
        }
        if (role == SystemRole.CORPORATE_EMPLOYEE || role == SystemRole.CORPORATE_SUPERVISOR) {
            if (!account.getAccountHolderId().equals(requestingUser.getRelatedid())) {
                throw new SecurityException("No tiene permiso para ver las cuentas de otra empresa.");
            }
        }
    }
}
