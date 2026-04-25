package app.application.adapters.api.controllers;

import app.application.adapters.api.request.RegisterIndividualCustomerRequest;
import app.application.adapters.api.response.RegisterIndividualCustomerResponse;
import app.application.usecases.IndividualCustomerUseCase;
import app.domain.models.IndividualCustomer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers/individual")
public class IndividualCustomerController {

    private final IndividualCustomerUseCase individualCustomerUseCase;

    public IndividualCustomerController(IndividualCustomerUseCase individualCustomerUseCase) {
        this.individualCustomerUseCase = individualCustomerUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterIndividualCustomerResponse> register(
            @Valid @RequestBody RegisterIndividualCustomerRequest request) {

        IndividualCustomer customer = new IndividualCustomer();
        customer.setName(request.getName());
        customer.setIdentificationId(request.getIdentificationId());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setBirthDate(request.getBirthDate());
        customer.setAddress(request.getAddress());

        IndividualCustomer registered = individualCustomerUseCase.register(
                customer, request.getUsername(), request.getPassword());

        RegisterIndividualCustomerResponse response = new RegisterIndividualCustomerResponse(
                registered.getName(),
                registered.getIdentificationId(),
                registered.getEmail(),
                registered.getPhone(),
                registered.getBirthDate(),
                registered.getAddress(),
                registered.getSystemRole(),
                request.getUsername()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}