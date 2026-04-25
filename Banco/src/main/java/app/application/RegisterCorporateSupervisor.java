package app.application;

import app.domain.models.User;
import app.domain.models.enums.SystemRole;
import app.domain.models.enums.UserStatus;
import app.domain.ports.IUserPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class RegisterCorporateSupervisor {

    private final IUserPort userPort;

    public RegisterCorporateSupervisor(IUserPort userPort) {
        this.userPort = userPort;
    }

    public User register(User supervisor, String username, String password) {
        validateFields(supervisor, username, password);
        validateAge(supervisor.getBirthDate());
        validateEmail(supervisor.getEmail());
        validatePhone(supervisor.getPhone());
        validateUniqueId(supervisor.getIdentificationId());
        validateCompany(supervisor.getRelatedId());

        supervisor.setSystemRole(SystemRole.CORPORATE_SUPERVISOR);
        supervisor.setUsername(username);
        supervisor.setPassword(password);
        supervisor.setUserStatus(UserStatus.ACTIVE);

        userPort.save(supervisor);
        return supervisor;
    }

    private void validateFields(User supervisor, String username, String password) {
        if (supervisor.getName() == null || supervisor.getName().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio.");
        if (supervisor.getIdentificationId() == null || supervisor.getIdentificationId().isBlank())
            throw new IllegalArgumentException("El número de identificación es obligatorio.");
        if (supervisor.getEmail() == null || supervisor.getEmail().isBlank())
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        if (supervisor.getPhone() == null || supervisor.getPhone().isBlank())
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        if (supervisor.getAddress() == null || supervisor.getAddress().isBlank())
            throw new IllegalArgumentException("La dirección es obligatoria.");
        if (supervisor.getBirthDate() == null)
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria.");
    }

    private void validateAge(LocalDate birthDate) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 18)
            throw new IllegalArgumentException("El supervisor debe ser mayor de edad (mínimo 18 años).");
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

    private void validateCompany(String relatedId) {
        if (relatedId == null || relatedId.isBlank())
            throw new IllegalArgumentException("El ID de la empresa es obligatorio.");
        if (!userPort.existsByIdentificationId(relatedId))
            throw new IllegalArgumentException(
                "No existe una empresa registrada con el ID: " + relatedId);
    }
}