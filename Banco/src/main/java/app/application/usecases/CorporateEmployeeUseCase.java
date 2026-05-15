package app.application.usecases;

import app.application.RegisterCorporateEmployee;
import app.domain.models.Transfer;
import app.domain.models.User;
import app.domain.ports.IUserPort;
import app.domain.services.interfaces.ITransferService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CorporateEmployeeUseCase {

    private final RegisterCorporateEmployee registerCorporateEmployee;
    private final ITransferService transferService;
    private final IUserPort userPort;

    public CorporateEmployeeUseCase(RegisterCorporateEmployee registerCorporateEmployee,
                                     ITransferService transferService,
                                     IUserPort userPort) {
        this.registerCorporateEmployee = registerCorporateEmployee;
        this.transferService = transferService;
        this.userPort = userPort;
    }

    public User register(User employee, String username, String password) {
        return registerCorporateEmployee.register(employee, username, password);
    }

    public Transfer createTransfer(Transfer transfer, String employeeIdentificationId) {
        User employee = userPort.findByIdentificationId(employeeIdentificationId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
        return transferService.createTransfer(transfer, employee);
    }

    public List<Transfer> getTransferHistory(String accountNumber,
                                              String employeeIdentificationId) {
        User employee = userPort.findByIdentificationId(employeeIdentificationId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
        return transferService.getTransferHistory(accountNumber, employee);
    }
}
