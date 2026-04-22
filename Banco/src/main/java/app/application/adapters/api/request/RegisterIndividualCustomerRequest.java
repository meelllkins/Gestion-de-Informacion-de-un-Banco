package app.application.adapters.api.request;

import app.domain.models.IndividualCustomer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterIndividualCustomerRequest {
    private IndividualCustomer customer;
    private String username;
    private String password;
}