package app.application.adapters.api.controllers;

import app.application.adapters.api.request.RegisterCorporateSupervisorRequest;
import app.application.adapters.api.response.RegisterCorporateSupervisorResponse;
import app.application.adapters.api.response.TransferResponse;
import app.application.usecases.CorporateSupervisorUseCase;
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
@RequestMapping("/api/employees/supervisor")
public class CorporateSupervisorController {

    private final CorporateSupervisorUseCase corporateSupervisorUseCase;

    public CorporateSupervisorController(CorporateSupervisorUseCase corporateSupervisorUseCase) {
        this.corporateSupervisorUseCase = corporateSupervisorUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterCorporateSupervisorResponse> register(
            @Valid @RequestBody RegisterCorporateSupervisorRequest request) {

        User supervisor = new User();
        supervisor.setName(request.getName());
        supervisor.setIdentificationId(request.getIdentificationId());
        supervisor.setEmail(request.getEmail());
        supervisor.setPhone(request.getPhone());
        supervisor.setAddress(request.getAddress());
        supervisor.setBirthDate(request.getBirthDate());
        supervisor.setRelatedId(request.getRelatedId());

        User registered = corporateSupervisorUseCase.register(
                supervisor, request.getUsername(), request.getPassword());

        RegisterCorporateSupervisorResponse response = new RegisterCorporateSupervisorResponse(
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

    @GetMapping("/pending-transfers")
    public ResponseEntity<List<TransferResponse>> getPendingTransfers() {

        String supervisorId = (String) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

        List<TransferResponse> response = corporateSupervisorUseCase
                .getPendingTransfers(supervisorId)
                .stream()
                .map(this::toTransferResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/approve-transfer/{transferId}")
    public ResponseEntity<TransferResponse> approveTransfer(@PathVariable int transferId) {

        String supervisorId = (String) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

        Transfer transfer = corporateSupervisorUseCase.approveTransfer(transferId, supervisorId);

        return ResponseEntity.ok(toTransferResponse(transfer));
    }

    @PutMapping("/reject-transfer/{transferId}")
    public ResponseEntity<TransferResponse> rejectTransfer(@PathVariable int transferId) {

        String supervisorId = (String) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

        Transfer transfer = corporateSupervisorUseCase.rejectTransfer(transferId, supervisorId);

        return ResponseEntity.ok(toTransferResponse(transfer));
    }

    private TransferResponse toTransferResponse(Transfer t) {
        return new TransferResponse(
                t.getTransferId(), t.getSourceAccount(), t.getDestinationAccount(),
                t.getAmount(), t.getCreationDate(), t.getApprovalDate(),
                t.getTransferStatus(), t.getCreatorUserId()
        );
    }
}
