package app.domain.services.implementations;

import app.domain.models.BankAccount;
import app.domain.models.User;
import app.domain.models.enums.SystemRole;
import app.domain.ports.IAccountPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAccountsByHolder {

    private final IAccountPort accountPort;

    @Autowired
    public GetAccountsByHolder(IAccountPort accountPort) {
        this.accountPort = accountPort;
    }

    public List<BankAccount> getAccountsByHolder(String holderId, User requestingUser) {
        SystemRole role = requestingUser.getSystemRole();
        if (role == SystemRole.INDIVIDUAL_CUSTOMER ||
            role == SystemRole.CORPORATE_CUSTOMER ||
            role == SystemRole.CORPORATE_EMPLOYEE) {
            if (!requestingUser.getIdentificationId().equals(holderId) &&
                !requestingUser.getRelatedid().equals(holderId)) {
                throw new SecurityException("No tiene permiso para ver las cuentas de otro cliente.");
            }
        }
        return accountPort.findByHolderId(holderId);
    }
}