package app.application.adapters.api.controllers;

import app.application.adapters.api.request.RegisterCorporateSupervisorRequest;
import app.application.adapters.api.response.RegisterCorporateSupervisorResponse;
import app.application.usecases.CorporateSupervisorUseCase;
import app.domain.models.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}