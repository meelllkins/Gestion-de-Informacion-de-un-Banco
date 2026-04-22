package app.domain.services.implementations;

import app.domain.enums.SystemRole;
import app.domain.models.User;
import org.springframework.stereotype.Service;

@Service
public class ValidateRole {

    public void validate(User user, SystemRole... allowedRoles) {
        for (SystemRole role : allowedRoles) {
            if (user.getSystemRole() == role) {
                return;
            }
        }
        throw new SecurityException(
            "El usuario con rol " + user.getSystemRole() +
            " no tiene permiso para realizar esta operación.");
    }
}