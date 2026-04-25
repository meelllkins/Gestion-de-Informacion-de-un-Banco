package app.application.adapters.api.controllers;

import app.application.adapters.api.request.RegisterTellerEmployeeRequest;
import app.application.adapters.api.response.RegisterTellerEmployeeResponse;
import app.application.usecases.TellerEmployeeUseCase;
import app.domain.models.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}