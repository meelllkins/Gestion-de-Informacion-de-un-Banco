package app.application.usecases;

import app.application.RegisterIndividualCustomer;
import app.domain.models.IndividualCustomer;
import org.springframework.stereotype.Service;

@Service
public class IndividualCustomerUseCase {

    private final RegisterIndividualCustomer registerIndividualCustomer;

    public IndividualCustomerUseCase(RegisterIndividualCustomer registerIndividualCustomer) {
        this.registerIndividualCustomer = registerIndividualCustomer;
    }

    public IndividualCustomer register(IndividualCustomer customer,
                                    String username, String password) {
        return registerIndividualCustomer.register(customer, username, password);
    }
}