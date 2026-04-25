package app.application.usecases;

import app.application.RegisterCorporateEmployee;
import app.domain.models.User;
import org.springframework.stereotype.Service;

@Service
public class CorporateEmployeeUseCase {

    private final RegisterCorporateEmployee registerCorporateEmployee;

    public CorporateEmployeeUseCase(RegisterCorporateEmployee registerCorporateEmployee) {
        this.registerCorporateEmployee = registerCorporateEmployee;
    }

    public User register(User employee, String username, String password) {
        return registerCorporateEmployee.register(employee, username, password);
    }
}