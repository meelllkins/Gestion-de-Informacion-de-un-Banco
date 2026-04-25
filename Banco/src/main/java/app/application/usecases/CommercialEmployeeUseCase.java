package app.application.usecases;

import app.application.RegisterCommercialEmployee;
import app.domain.models.User;
import org.springframework.stereotype.Service;

@Service
public class CommercialEmployeeUseCase {

    private final RegisterCommercialEmployee registerCommercialEmployee;

    public CommercialEmployeeUseCase(RegisterCommercialEmployee registerCommercialEmployee) {
        this.registerCommercialEmployee = registerCommercialEmployee;
    }

    public User register(User employee, String username, String password) {
        return registerCommercialEmployee.register(employee, username, password);
    }
}