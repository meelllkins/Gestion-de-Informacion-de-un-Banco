package app.application.adapters.api.controllers;

import app.application.adapters.api.request.RegisterCorporateCustomerRequest;
import app.application.adapters.api.response.RegisterCorporateCustomerResponse;
import app.application.usecases.CorporateCustomerUseCase;
import app.domain.models.CorporateCustomer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers/corporate")
public class CorporateCustomerController {

    private final CorporateCustomerUseCase corporateCustomerUseCase;

    public CorporateCustomerController(CorporateCustomerUseCase corporateCustomerUseCase) {
        this.corporateCustomerUseCase = corporateCustomerUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterCorporateCustomerResponse> register(
            @Valid @RequestBody RegisterCorporateCustomerRequest request) {

        CorporateCustomer customer = new CorporateCustomer();
        customer.setBusinessName(request.getBusinessName());
        customer.setIdentificationId(request.getIdentificationId());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setLegalRepresentative(request.getLegalRepresentative());

        CorporateCustomer registered = corporateCustomerUseCase.register(
                customer, request.getUsername(), request.getPassword());

        RegisterCorporateCustomerResponse response = new RegisterCorporateCustomerResponse(
                registered.getBusinessName(),
                registered.getIdentificationId(),
                registered.getEmail(),
                registered.getPhone(),
                registered.getAddress(),
                registered.getLegalRepresentative(),
                registered.getSystemRole(),
                request.getUsername()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}