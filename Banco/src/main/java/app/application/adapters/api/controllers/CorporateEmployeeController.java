package app.application.adapters.api.controllers;

import app.application.adapters.api.request.BulkTransferRequest;
import app.application.adapters.api.request.CreateTransferRequest;
import app.application.adapters.api.request.RegisterCorporateEmployeeRequest;
import app.application.adapters.api.response.RegisterCorporateEmployeeResponse;
import app.application.adapters.api.response.TransferResponse;
import app.application.usecases.CorporateEmployeeUseCase;
import app.domain.models.Transfer;
import app.domain.models.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees/corporate")
public class CorporateEmployeeController {

    private final CorporateEmployeeUseCase corporateEmployeeUseCase;

    public CorporateEmployeeController(CorporateEmployeeUseCase corporateEmployeeUseCase) {
        this.corporateEmployeeUseCase = corporateEmployeeUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterCorporateEmployeeResponse> register(
            @Valid @RequestBody RegisterCorporateEmployeeRequest request) {

        User employee = new User();
        employee.setName(request.getName());
        employee.setIdentificationId(request.getIdentificationId());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());
        employee.setBirthDate(request.getBirthDate());
        employee.setRelatedId(request.getRelatedId());

        User registered = corporateEmployeeUseCase.register(
                employee, request.getUsername(), request.getPassword());

        RegisterCorporateEmployeeResponse response = new RegisterCorporateEmployeeResponse(
                registered.getName(),
                registered.getIdentificationId(),
                registered.getEmail(),
                registered.getPhone(),
                registered.getAddress(),
                registered.getBirthDate(),
                registered.getRelatedId(),
                registered.getSystemRole(),
                request.getUsername()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> createTransfer(
            @Valid @RequestBody CreateTransferRequest request) {

        String employeeId = (String) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

        Transfer transfer = new Transfer();
        transfer.setSourceAccount(request.getSourceAccount());
        transfer.setDestinationAccount(request.getDestinationAccount());
        transfer.setAmount(request.getAmount());

        Transfer created = corporateEmployeeUseCase.createTransfer(transfer, employeeId);

        return ResponseEntity.status(HttpStatus.CREATED).body(toTransferResponse(created));
    }

    @PostMapping("/bulk-transfer")
    public ResponseEntity<List<TransferResponse>> createBulkTransfer(
            @Valid @RequestBody BulkTransferRequest request) {

        String employeeId = (String) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

        List<Transfer> transfers = request.getTransfers().stream()
                .map(r -> {
                    Transfer t = new Transfer();
                    t.setSourceAccount(r.getSourceAccount());
                    t.setDestinationAccount(r.getDestinationAccount());
                    t.setAmount(r.getAmount());
                    return t;
                })
                .collect(Collectors.toList());

        List<TransferResponse> response = corporateEmployeeUseCase
                .createBulkTransfer(transfers, employeeId)
                .stream()
                .map(this::toTransferResponse)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-transfers/{accountNumber}")
    public ResponseEntity<List<TransferResponse>> getMyTransfers(
            @PathVariable String accountNumber) {

        String employeeId = (String) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

        List<TransferResponse> response = corporateEmployeeUseCase
                .getTransferHistory(accountNumber, employeeId)
                .stream()
                .map(this::toTransferResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private TransferResponse toTransferResponse(Transfer t) {
        return new TransferResponse(
                t.getTransferId(), t.getSourceAccount(), t.getDestinationAccount(),
                t.getAmount(), t.getCreationDate(), t.getApprovalDate(),
                t.getTransferStatus(), t.getCreatorUserId()
        );
    }
}
