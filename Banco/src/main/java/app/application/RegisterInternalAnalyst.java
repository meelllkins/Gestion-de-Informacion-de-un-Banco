package app.application;

import app.domain.models.User;
import app.domain.models.enums.SystemRole;
import app.domain.models.enums.UserStatus;
import app.domain.ports.IUserPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class RegisterInternalAnalyst {

    private final IUserPort userPort;

    public RegisterInternalAnalyst(IUserPort userPort) {
        this.userPort = userPort;
    }

    public User register(User analyst, String username, String password) {
        validateFields(analyst, username, password);
        validateAge(analyst.getBirthDate());
        validateEmail(analyst.getEmail());
        validatePhone(analyst.getPhone());
        validateUniqueId(analyst.getIdentificationId());

        analyst.setSystemRole(SystemRole.INTERNAL_ANALYST);
        analyst.setUsername(username);
        analyst.setPassword(password);
        analyst.setUserStatus(UserStatus.ACTIVE);

        userPort.save(analyst);
        return analyst;
    }

    private void validateFields(User analyst, String username, String password) {
        if (analyst.getName() == null || analyst.getName().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio.");
        if (analyst.getIdentificationId() == null || analyst.getIdentificationId().isBlank())
            throw new IllegalArgumentException("El número de identificación es obligatorio.");
        if (analyst.getEmail() == null || analyst.getEmail().isBlank())
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        if (analyst.getPhone() == null || analyst.getPhone().isBlank())
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        if (analyst.getAddress() == null || analyst.getAddress().isBlank())
            throw new IllegalArgumentException("La dirección es obligatoria.");
        if (analyst.getBirthDate() == null)
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria.");
    }

    private void validateAge(LocalDate birthDate) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 18)
            throw new IllegalArgumentException("El analista debe ser mayor de edad (mínimo 18 años).");
    }

    private void validateEmail(String email) {
        if (!email.contains("@") || !email.contains("."))
            throw new IllegalArgumentException("El correo no tiene formato válido.");
    }

    private void validatePhone(String phone) {
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() < 7 || digits.length() > 15)
            throw new IllegalArgumentException("El teléfono debe tener entre 7 y 15 dígitos.");
    }

    private void validateUniqueId(String identificationId) {
        if (userPort.existsByIdentificationId(identificationId))
            throw new IllegalArgumentException(
                "Ya existe un usuario con el número de identificación: " + identificationId);
    }
}