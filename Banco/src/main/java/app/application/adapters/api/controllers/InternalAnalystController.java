package app.application.adapters.api.controllers;

import app.application.adapters.api.request.RegisterInternalAnalystRequest;
import app.application.adapters.api.response.RegisterInternalAnalystResponse;
import app.application.usecases.InternalAnalystUseCase;
import app.domain.models.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}