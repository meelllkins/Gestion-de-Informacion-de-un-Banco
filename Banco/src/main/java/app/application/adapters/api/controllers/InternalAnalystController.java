package app.application.adapters.api.controllers;

import app.application.adapters.api.request.ApproveLoanRequest;
import app.application.adapters.api.request.DisburseRequest;
import app.application.adapters.api.request.RegisterInternalAnalystRequest;
import app.application.adapters.api.response.LoanResponse;
import app.application.adapters.api.response.RegisterInternalAnalystResponse;
import app.application.usecases.InternalAnalystUseCase;
import app.domain.models.Loan;
import app.domain.models.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees/analyst")
public class InternalAnalystController {

    private final InternalAnalystUseCase internalAnalystUseCase;

    public InternalAnalystController(InternalAnalystUseCase internalAnalystUseCase) {
        this.internalAnalystUseCase = internalAnalystUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterInternalAnalystResponse> register(
            @Valid @RequestBody RegisterInternalAnalystRequest request) {

        User analyst = new User();
        analyst.setName(request.getName());
        analyst.setIdentificationId(request.getIdentificationId());
        analyst.setEmail(request.getEmail());
        analyst.setPhone(request.getPhone());
        analyst.setAddress(request.getAddress());
        analyst.setBirthDate(request.getBirthDate());

        User registered = internalAnalystUseCase.register(
                analyst, request.getUsername(), request.getPassword());

        RegisterInternalAnalystResponse response = new RegisterInternalAnalystResponse(
                registered.getName(),
                registered.getIdentificationId(),
                registered.getEmail(),
                registered.getPhone(),
                registered.getAddress(),
                registered.getBirthDate(),
                registered.getSystemRole(),
                request.getUsername()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/pending-loans")
    public ResponseEntity<List<LoanResponse>> getPendingLoans() {

        String analystId = (String) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

        List<LoanResponse> response = internalAnalystUseCase.getPendingLoans(analystId)
                .stream()
                .map(this::toLoanResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/approve-loan/{loanId}")
    public ResponseEntity<LoanResponse> approveLoan(
            @PathVariable int loanId,
            @Valid @RequestBody ApproveLoanRequest request) {

        String analystId = (String) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

        Loan loan = internalAnalystUseCase.approveLoan(
                loanId, request.getApprovedAmount(), request.getInterestRate(), analystId);

        return ResponseEntity.ok(toLoanResponse(loan));
    }

    @PutMapping("/reject-loan/{loanId}")
    public ResponseEntity<LoanResponse> rejectLoan(@PathVariable int loanId) {

        String analystId = (String) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

        Loan loan = internalAnalystUseCase.rejectLoan(loanId, analystId);

        return ResponseEntity.ok(toLoanResponse(loan));
    }

    @PostMapping("/disburse-loan/{loanId}")
    public ResponseEntity<LoanResponse> disburseLoan(
            @PathVariable int loanId,
            @Valid @RequestBody DisburseRequest request) {

        String analystId = (String) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

        Loan loan = internalAnalystUseCase.disburseLoan(
                loanId, request.getDestinationAccount(), analystId);

        return ResponseEntity.ok(toLoanResponse(loan));
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
}
