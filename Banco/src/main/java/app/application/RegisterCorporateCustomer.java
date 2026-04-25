package app.application;

import app.domain.models.CorporateCustomer;
import app.domain.models.User;
import app.domain.models.enums.SystemRole;
import app.domain.models.enums.UserStatus;
import app.domain.ports.IUserPort;
import org.springframework.stereotype.Service;

@Service
public class RegisterCorporateCustomer {

    private final IUserPort userPort;

    public RegisterCorporateCustomer(IUserPort userPort) {
        this.userPort = userPort;
    }

    public CorporateCustomer register(CorporateCustomer customer,
                                      String username, String password) {
        validateFields(customer, username, password);
        validateEmail(customer.getEmail());
        validatePhone(customer.getPhone());
        validateUniqueId(customer.getIdentificationId());

        customer.setSystemRole(SystemRole.CORPORATE_CUSTOMER);

        User user = buildUser(customer, username, password);
        userPort.save(user);

        return customer;
    }

    private void validateFields(CorporateCustomer customer,
                                String username, String password) {
        if (customer.getBusinessName() == null || customer.getBusinessName().isBlank())
            throw new IllegalArgumentException("La razón social es obligatoria.");
        if (customer.getIdentificationId() == null || customer.getIdentificationId().isBlank())
            throw new IllegalArgumentException("El NIT es obligatorio.");
        if (customer.getEmail() == null || customer.getEmail().isBlank())
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        if (customer.getPhone() == null || customer.getPhone().isBlank())
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        if (customer.getAddress() == null || customer.getAddress().isBlank())
            throw new IllegalArgumentException("La dirección es obligatoria.");
        if (customer.getLegalRepresentative() == null || customer.getLegalRepresentative().isBlank())
            throw new IllegalArgumentException("El representante legal es obligatorio.");
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria.");
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
                "Ya existe un usuario con el NIT: " + identificationId);
    }

    private User buildUser(CorporateCustomer customer, String username, String password) {
        User user = new User();
        user.setName(customer.getBusinessName());
        user.setIdentificationId(customer.getIdentificationId());
        user.setEmail(customer.getEmail());
        user.setPhone(customer.getPhone());
        user.setAddress(customer.getAddress());
        user.setSystemRole(SystemRole.CORPORATE_CUSTOMER);
        user.setUsername(username);
        user.setPassword(password);
        user.setUserStatus(UserStatus.ACTIVE);
        return user;
    }
}