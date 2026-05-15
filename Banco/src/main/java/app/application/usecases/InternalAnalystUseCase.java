package app.application.usecases;

import app.application.RegisterInternalAnalyst;
import app.domain.models.Loan;
import app.domain.models.User;
import app.domain.ports.ILoanPort;
import app.domain.ports.IUserPort;
import app.domain.services.interfaces.ILoanService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InternalAnalystUseCase {

    private final RegisterInternalAnalyst registerInternalAnalyst;
    private final ILoanService loanService;
    private final ILoanPort loanPort;
    private final IUserPort userPort;

    public InternalAnalystUseCase(RegisterInternalAnalyst registerInternalAnalyst,
                                   ILoanService loanService,
                                   ILoanPort loanPort,
                                   IUserPort userPort) {
        this.registerInternalAnalyst = registerInternalAnalyst;
        this.loanService = loanService;
        this.loanPort = loanPort;
        this.userPort = userPort;
    }

    public User register(User analyst, String username, String password) {
        return registerInternalAnalyst.register(analyst, username, password);
    }

    public List<Loan> getPendingLoans(String analystIdentificationId) {
        User analyst = userPort.findByIdentificationId(analystIdentificationId)
                .orElseThrow(() -> new IllegalArgumentException("Analista no encontrado"));
        return loanService.getPendingLoans(analyst);
    }

    public Loan approveLoan(int loanId, double approvedAmount, double interestRate,
                            String analystIdentificationId) {
        User analyst = userPort.findByIdentificationId(analystIdentificationId)
                .orElseThrow(() -> new IllegalArgumentException("Analista no encontrado"));
        return loanService.approveLoan(loanId, approvedAmount, interestRate, analyst);
    }

    public Loan rejectLoan(int loanId, String analystIdentificationId) {
        User analyst = userPort.findByIdentificationId(analystIdentificationId)
                .orElseThrow(() -> new IllegalArgumentException("Analista no encontrado"));
        return loanService.rejectLoan(loanId, analyst);
    }

    public Loan disburseLoan(int loanId, String destinationAccount,
                             String analystIdentificationId) {
        User analyst = userPort.findByIdentificationId(analystIdentificationId)
                .orElseThrow(() -> new IllegalArgumentException("Analista no encontrado"));
        if (destinationAccount != null && !destinationAccount.isBlank()) {
            loanPort.updateDestinationAccount(loanId, destinationAccount);
        }
        return loanService.disburseLoan(loanId, analyst);
    }
}
