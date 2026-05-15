package app.application.usecases;

import app.application.RegisterCorporateSupervisor;
import app.domain.models.Transfer;
import app.domain.models.User;
import app.domain.ports.IUserPort;
import app.domain.services.interfaces.ITransferService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CorporateSupervisorUseCase {

    private final RegisterCorporateSupervisor registerCorporateSupervisor;
    private final ITransferService transferService;
    private final IUserPort userPort;

    public CorporateSupervisorUseCase(RegisterCorporateSupervisor registerCorporateSupervisor,
                                       ITransferService transferService,
                                       IUserPort userPort) {
        this.registerCorporateSupervisor = registerCorporateSupervisor;
        this.transferService = transferService;
        this.userPort = userPort;
    }

    public User register(User supervisor, String username, String password) {
        return registerCorporateSupervisor.register(supervisor, username, password);
    }

    public List<Transfer> getPendingTransfers(String supervisorIdentificationId) {
        User supervisor = userPort.findByIdentificationId(supervisorIdentificationId)
                .orElseThrow(() -> new IllegalArgumentException("Supervisor no encontrado"));
        return transferService.getPendingApprovalTransfers(supervisor);
    }

    public Transfer approveTransfer(int transferId, String supervisorIdentificationId) {
        User supervisor = userPort.findByIdentificationId(supervisorIdentificationId)
                .orElseThrow(() -> new IllegalArgumentException("Supervisor no encontrado"));
        return transferService.approveTransfer(transferId, supervisor);
    }

    public Transfer rejectTransfer(int transferId, String supervisorIdentificationId) {
        User supervisor = userPort.findByIdentificationId(supervisorIdentificationId)
                .orElseThrow(() -> new IllegalArgumentException("Supervisor no encontrado"));
        return transferService.rejectTransfer(transferId, supervisor);
    }
}
