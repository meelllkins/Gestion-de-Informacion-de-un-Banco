package app.application.usecases;

import app.application.RegisterIndividualCustomer;
import app.domain.models.BankAccount;
import app.domain.models.IndividualCustomer;
import app.domain.models.Loan;
import app.domain.models.Transfer;
import app.domain.models.User;
import app.domain.ports.IUserPort;
import app.domain.services.implementations.GetAccountsByHolder;
import app.domain.services.interfaces.ILoanService;
import app.domain.services.interfaces.ITransferService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndividualCustomerUseCase {

    private final RegisterIndividualCustomer registerIndividualCustomer;
    private final ILoanService loanService;
    private final ITransferService transferService;
    private final GetAccountsByHolder accountsByHolderService;
    private final IUserPort userPort;

    public IndividualCustomerUseCase(RegisterIndividualCustomer registerIndividualCustomer,
                                     ILoanService loanService,
                                     ITransferService transferService,
                                     GetAccountsByHolder accountsByHolderService,
                                     IUserPort userPort) {
        this.registerIndividualCustomer = registerIndividualCustomer;
        this.loanService = loanService;
        this.transferService = transferService;
        this.accountsByHolderService = accountsByHolderService;
        this.userPort = userPort;
    }

    public IndividualCustomer register(IndividualCustomer customer,
                                       String username, String password) {
        return registerIndividualCustomer.register(customer, username, password);
    }

    public Loan requestLoan(Loan loan, String customerIdentificationId) {
        User customer = userPort.findByIdentificationId(customerIdentificationId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        return loanService.requestLoan(loan, customer);
    }

    public Transfer createTransfer(Transfer transfer, String customerIdentificationId) {
        User customer = userPort.findByIdentificationId(customerIdentificationId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        return transferService.createTransfer(transfer, customer);
    }

    public List<BankAccount> getMyAccounts(String customerIdentificationId) {
        User customer = userPort.findByIdentificationId(customerIdentificationId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        return accountsByHolderService.getAccountsByHolder(customerIdentificationId, customer);
    }
}
