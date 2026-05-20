package app.application.adapters.api.controllers;

import app.application.adapters.api.request.DepositWithdrawRequest;
import app.application.adapters.api.request.OpenAccountRequest;
import app.application.adapters.api.request.RegisterTellerEmployeeRequest;
import app.application.adapters.api.response.AccountSummaryResponse;
import app.application.adapters.api.response.OpenAccountResponse;
import app.application.adapters.api.response.RegisterTellerEmployeeResponse;
import app.application.usecases.TellerEmployeeUseCase;
import app.domain.models.BankAccount;
import app.domain.models.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees/teller")
public class TellerEmployeeController {

    private final TellerEmployeeUseCase tellerEmployeeUseCase;

    public TellerEmployeeController(TellerEmployeeUseCase tellerEmployeeUseCase) {
        this.tellerEmployeeUseCase = tellerEmployeeUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterTellerEmployeeResponse> register(
            @Valid @RequestBody RegisterTellerEmployeeRequest request) {

        User employee = new User();
        employee.setName(request.getName());
        employee.setIdentificationId(request.getIdentificationId());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());
        employee.setBirthDate(request.getBirthDate());

        User registered = tellerEmployeeUseCase.register(
                employee, request.getUsername(), request.getPassword());

        RegisterTellerEmployeeResponse response = new RegisterTellerEmployeeResponse(
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

    @PostMapping("/open-account")
    public ResponseEntity<OpenAccountResponse> openAccount(
            @Valid @RequestBody OpenAccountRequest request) {

        String tellerId = (String) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

        BankAccount account = new BankAccount();
        account.setAccountNumber(request.getAccountNumber());
        account.setAccountType(request.getAccountType());
        account.setAccountHolderId(request.getAccountHolderId());
        account.setBalance(request.getBalance());
        account.setCurrency(request.getCurrency());
        account.setProductCode(request.getProductCode());
        account.setProductName(request.getProductName());
        account.setCategory(request.getCategory());
        account.setRequiresApproval(request.isRequiresApproval());

        BankAccount opened = tellerEmployeeUseCase.openAccount(account, tellerId);

        return ResponseEntity.status(HttpStatus.CREATED).body(new OpenAccountResponse(
                opened.getAccountNumber(), opened.getAccountType(),
                opened.getAccountHolderId(), opened.getBalance(),
                opened.getCurrency(), opened.getAccountStatus(),
                opened.getOpeningDate(), opened.getProductCode(),
                opened.getProductName(), opened.getCategory()
        ));
    }

    @PostMapping("/deposit")
    public ResponseEntity<Void> deposit(@Valid @RequestBody DepositWithdrawRequest request) {
        String tellerId = (String) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();
        tellerEmployeeUseCase.deposit(request.getAccountNumber(), request.getAmount(), tellerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@Valid @RequestBody DepositWithdrawRequest request) {
        String tellerId = (String) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();
        tellerEmployeeUseCase.withdraw(request.getAccountNumber(), request.getAmount(), tellerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/account-balance/{identificationId}")
    public ResponseEntity<List<AccountSummaryResponse>> getAccountBalance(
            @PathVariable String identificationId) {

        String tellerId = (String) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

        List<AccountSummaryResponse> response = tellerEmployeeUseCase
                .getAccountsByHolder(identificationId, tellerId)
                .stream()
                .map(a -> new AccountSummaryResponse(
                        a.getAccountNumber(), a.getAccountType(),
                        a.getAccountHolderId(), a.getBalance(),
                        a.getCurrency(), a.getAccountStatus(),
                        a.getOpeningDate()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
