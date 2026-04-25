package app.application.adapters.api.controllers;

import app.application.adapters.api.request.RegisterCommercialEmployeeRequest;
import app.application.adapters.api.response.RegisterCommercialEmployeeResponse;
import app.application.usecases.CommercialEmployeeUseCase;
import app.domain.models.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees/commercial")
public class CommercialEmployeeController {

    private final CommercialEmployeeUseCase commercialEmployeeUseCase;

    public CommercialEmployeeController(CommercialEmployeeUseCase commercialEmployeeUseCase) {
        this.commercialEmployeeUseCase = commercialEmployeeUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterCommercialEmployeeResponse> register(
            @Valid @RequestBody RegisterCommercialEmployeeRequest request) {

        User employee = new User();
        employee.setName(request.getName());
        employee.setIdentificationId(request.getIdentificationId());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());
        employee.setBirthDate(request.getBirthDate());

        User registered = commercialEmployeeUseCase.register(
                employee, request.getUsername(), request.getPassword());

        RegisterCommercialEmployeeResponse response = new RegisterCommercialEmployeeResponse(
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