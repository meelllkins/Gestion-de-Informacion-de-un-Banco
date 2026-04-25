package app.application;

import app.domain.models.User;
import app.domain.models.enums.SystemRole;
import app.domain.models.enums.UserStatus;
import app.domain.ports.IUserPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class RegisterTellerEmployee {

    private final IUserPort userPort;

    public RegisterTellerEmployee(IUserPort userPort) {
        this.userPort = userPort;
    }

    public User register(User employee, String username, String password) {
        validateFields(employee, username, password);
        validateAge(employee.getBirthDate());
        validateEmail(employee.getEmail());
        validatePhone(employee.getPhone());
        validateUniqueId(employee.getIdentificationId());

        employee.setSystemRole(SystemRole.TELLER_EMPLOYEE);
        employee.setUsername(username);
        employee.setPassword(password);
        employee.setUserStatus(UserStatus.ACTIVE);

        userPort.save(employee);
        return employee;
    }

    private void validateFields(User employee, String username, String password) {
        if (employee.getName() == null || employee.getName().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio.");
        if (employee.getIdentificationId() == null || employee.getIdentificationId().isBlank())
            throw new IllegalArgumentException("El número de identificación es obligatorio.");
        if (employee.getEmail() == null || employee.getEmail().isBlank())
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        if (employee.getPhone() == null || employee.getPhone().isBlank())
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        if (employee.getAddress() == null || employee.getAddress().isBlank())
            throw new IllegalArgumentException("La dirección es obligatoria.");
        if (employee.getBirthDate() == null)
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria.");
    }

    private void validateAge(LocalDate birthDate) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 18)
            throw new IllegalArgumentException("El empleado debe ser mayor de edad (mínimo 18 años).");
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