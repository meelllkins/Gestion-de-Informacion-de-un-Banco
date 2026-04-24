package app.application.adapters.api.controllers;

import app.domain.models.IndividualCustomer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import app.application.RegisterIndividualCustomer;
import app.application.adapters.api.request.RegisterIndividualCustomerRequest;

@RestController
@RequestMapping("/api/customers/individual")
public class IndividualCustomerController {

    private final RegisterIndividualCustomer registerIndividualCustomer;

    
    public IndividualCustomerController(RegisterIndividualCustomer registerIndividualCustomer) {
        this.registerIndividualCustomer = registerIndividualCustomer;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterIndividualCustomerRequest request) {
        try {
            IndividualCustomer customer = registerIndividualCustomer.register(
                request.getCustomer(),
                request.getUsername(),
                request.getPassword()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(customer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}