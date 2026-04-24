package app.application.usecases;

import app.application.RegisterCorporateCustomer;
import app.domain.models.CorporateCustomer;
import org.springframework.stereotype.Service;

@Service
public class CorporateCustomerUseCase {

    private final RegisterCorporateCustomer registerCorporateCustomer;

    public CorporateCustomerUseCase(RegisterCorporateCustomer registerCorporateCustomer) {
        this.registerCorporateCustomer = registerCorporateCustomer;
    }

    public CorporateCustomer register(CorporateCustomer customer,
                                      String username, String password) {
        return registerCorporateCustomer.register(customer, username, password);
    }
}