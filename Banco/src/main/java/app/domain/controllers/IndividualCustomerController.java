package app.domain.controllers;

import app.domain.application.RegisterIndividualCustomer;
import app.domain.models.IndividualCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import app.domain.dto.RegisterIndividualCustomerRequest;

@RestController
@RequestMapping("/api/customers/individual")
public class IndividualCustomerController {

    private final RegisterIndividualCustomer registerIndividualCustomer;

    @Autowired
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