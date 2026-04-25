package app.application.usecases;

import app.application.RegisterInternalAnalyst;
import app.domain.models.User;
import org.springframework.stereotype.Service;

@Service
public class InternalAnalystUseCase {

    private final RegisterInternalAnalyst registerInternalAnalyst;

    public InternalAnalystUseCase(RegisterInternalAnalyst registerInternalAnalyst) {
        this.registerInternalAnalyst = registerInternalAnalyst;
    }

    public User register(User analyst, String username, String password) {
        return registerInternalAnalyst.register(analyst, username, password);
    }
}