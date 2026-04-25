package app.application.adapters.api.controllers;

import app.application.adapters.api.request.RegisterCorporateEmployeeRequest;
import app.application.adapters.api.response.RegisterCorporateEmployeeResponse;
import app.application.usecases.CorporateEmployeeUseCase;
import app.domain.models.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}