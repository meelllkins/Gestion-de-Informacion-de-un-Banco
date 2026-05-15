package app.application.adapters.api.controllers;

import app.application.adapters.api.request.CreateTransferRequest;
import app.application.adapters.api.request.RegisterIndividualCustomerRequest;
import app.application.adapters.api.request.RequestLoanRequest;
import app.application.adapters.api.response.AccountSummaryResponse;
import app.application.adapters.api.response.LoanResponse;
import app.application.adapters.api.response.RegisterIndividualCustomerResponse;
import app.application.adapters.api.response.TransferResponse;
import app.application.usecases.IndividualCustomerUseCase;
import app.domain.models.IndividualCustomer;
import app.domain.models.Loan;
import app.domain.models.Transfer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers/individual")
public class IndividualCustomerController {

    private final IndividualCustomerUseCase individualCustomerUseCase;

    public IndividualCustomerController(IndividualCustomerUseCase individualCustomerUseCase) {
        this.individualCustomerUseCase = individualCustomerUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterIndividualCustomerResponse> register(
            @Valid @RequestBody RegisterIndividualCustomerRequest request) {

        IndividualCustomer customer = new IndividualCustomer();
        customer.setName(request.getName());
        customer.setIdentificationId(request.getIdentificationId());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setBirthDate(request.getBirthDate());
        customer.setAddress(request.getAddress());

        IndividualCustomer registered = individualCustomerUseCase.register(
                customer, request.getUsername(), request.getPassword());

        RegisterIndividualCustomerResponse response = new RegisterIndividualCustomerResponse(
                registered.getName(),
                registered.getIdentificationId(),
                registered.getEmail(),
                registered.getPhone(),
                registered.getBirthDate(),
                registered.getAddress(),
                registered.getSystemRole(),
                request.getUsername()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/request-loan")
    public ResponseEntity<LoanResponse> requestLoan(
            @Valid @RequestBody RequestLoanRequest request) {

        String customerId = (String) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

        Loan loan = new Loan();
        loan.setLoanType(request.getLoanType());
        loan.setApplicantClientId(customerId);
        loan.setRequestedAmount(request.getRequestedAmount());
        loan.setTermMonths(request.getTermMonths());
        loan.setProductCode(request.getProductCode());
        loan.setProductName(request.getProductName());
        loan.setCategory(request.getCategory());
        loan.setDestinationAccount(request.getDestinationAccount());

        Loan created = individualCustomerUseCase.requestLoan(loan, customerId);

        return ResponseEntity.status(HttpStatus.CREATED).body(toLoanResponse(created));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> createTransfer(
            @Valid @RequestBody CreateTransferRequest request) {

        String customerId = (String) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

        Transfer transfer = new Transfer();
        transfer.setSourceAccount(request.getSourceAccount());
        transfer.setDestinationAccount(request.getDestinationAccount());
        transfer.setAmount(request.getAmount());

        Transfer created = individualCustomerUseCase.createTransfer(transfer, customerId);

        return ResponseEntity.status(HttpStatus.CREATED).body(toTransferResponse(created));
    }

    @GetMapping("/my-accounts")
    public ResponseEntity<List<AccountSummaryResponse>> getMyAccounts() {

        String customerId = (String) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

        List<AccountSummaryResponse> response = individualCustomerUseCase
                .getMyAccounts(customerId)
                .stream()
                .map(a -> new AccountSummaryResponse(
                        a.getAccountNumber(), a.getAccountType(),
                        a.getAccountHolderId(), a.getBalance(),
                        a.getCurrency(), a.getAccountStatus(),
                        a.getOpeningDate()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private LoanResponse toLoanResponse(Loan loan) {
        return new LoanResponse(
                loan.getLoanId(), loan.getLoanType(), loan.getApplicantClientId(),
                loan.getRequestedAmount(), loan.getApprovedAmount(), loan.getInterestRate(),
                loan.getTermMonths(), loan.getApprovalDate(), loan.getDisbursementDate(),
                loan.getDestinationAccount(), loan.getLoanStatus(),
                loan.getProductCode(), loan.getProductName(), loan.getCategory()
        );
    }

    private TransferResponse toTransferResponse(Transfer t) {
        return new TransferResponse(
                t.getTransferId(), t.getSourceAccount(), t.getDestinationAccount(),
                t.getAmount(), t.getCreationDate(), t.getApprovalDate(),
                t.getTransferStatus(), t.getCreatorUserId()
        );
    }
}
