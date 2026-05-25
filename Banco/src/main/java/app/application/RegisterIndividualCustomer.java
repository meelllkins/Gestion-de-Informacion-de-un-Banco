package app.application;

import app.domain.models.IndividualCustomer;
import app.domain.models.User;
import app.domain.models.enums.SystemRole;
import app.domain.models.enums.UserStatus;
import app.domain.ports.IUserPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class RegisterIndividualCustomer {

    private final IUserPort userPort;

    public RegisterIndividualCustomer(IUserPort userPort) {
        this.userPort = userPort;
    }

    public IndividualCustomer register(IndividualCustomer customer,
                            String username, String password) {
        validateFields(customer, username, password);
        validateAge(customer.getBirthDate());
        validateEmail(customer.getEmail());
        validatePhone(customer.getPhone());
        validateUniqueId(customer.getIdentificationId());

        customer.setSystemRole(SystemRole.INDIVIDUAL_CUSTOMER);

        User user = buildUser(customer, username, password);
        userPort.save(user);

        return customer;
    }

private void validateFields(IndividualCustomer customer, String username, String password) {
        if (customer.getName() == null || customer.getName().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio.");
        if (customer.getIdentificationId() == null || customer.getIdentificationId().isBlank())
            throw new IllegalArgumentException("El número de identificación es obligatorio.");
        if (customer.getEmail() == null || customer.getEmail().isBlank())
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        if (customer.getPhone() == null || customer.getPhone().isBlank())
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        if (customer.getAddress() == null || customer.getAddress().isBlank())
            throw new IllegalArgumentException("La dirección es obligatoria.");
        if (customer.getBirthDate() == null)
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria.");
    }

    private void validateAge(LocalDate birthDate) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 18)
            throw new IllegalArgumentException("El cliente debe ser mayor de edad (mínimo 18 años).");
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

    private User buildUser(IndividualCustomer customer, String username, String password) {
        User user = new User();
        user.setName(customer.getName());
        user.setIdentificationId(customer.getIdentificationId());
        user.setEmail(customer.getEmail());
        user.setPhone(customer.getPhone());
        user.setBirthDate(customer.getBirthDate());
        user.setAddress(customer.getAddress());
        user.setSystemRole(SystemRole.INDIVIDUAL_CUSTOMER);
        user.setUsername(username);
        user.setPassword(password);
        user.setUserStatus(UserStatus.ACTIVE);
        return user;
    }
}