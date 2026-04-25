package app.application.usecases;

import app.application.RegisterTellerEmployee;
import app.domain.models.User;
import org.springframework.stereotype.Service;

@Service
public class TellerEmployeeUseCase {

    private final RegisterTellerEmployee registerTellerEmployee;

    public TellerEmployeeUseCase(RegisterTellerEmployee registerTellerEmployee) {
        this.registerTellerEmployee = registerTellerEmployee;
    }

    public User register(User employee, String username, String password) {
        return registerTellerEmployee.register(employee, username, password);
    }
}