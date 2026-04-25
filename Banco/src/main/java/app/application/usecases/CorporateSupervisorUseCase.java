package app.application.usecases;

import app.application.RegisterCorporateSupervisor;
import app.domain.models.User;
import org.springframework.stereotype.Service;

@Service
public class CorporateSupervisorUseCase {

    private final RegisterCorporateSupervisor registerCorporateSupervisor;

    public CorporateSupervisorUseCase(RegisterCorporateSupervisor registerCorporateSupervisor) {
        this.registerCorporateSupervisor = registerCorporateSupervisor;
    }

    public User register(User supervisor, String username, String password) {
        return registerCorporateSupervisor.register(supervisor, username, password);
    }
}