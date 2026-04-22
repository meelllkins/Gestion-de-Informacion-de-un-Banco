package app.domain.services.implementations;

import app.domain.models.CorporateCustomer;
import app.domain.models.IndividualCustomer;
import app.domain.models.User;
import app.domain.models.enums.SystemRole;
import app.domain.models.enums.UserStatus;
import app.domain.services.interfaces.IAuthService;
import app.domain.services.interfaces.IUserService;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IUserService {

    private final List<User> users = new ArrayList<>();
    private final IAuthService authService;

    public UserService(IAuthService authService) {
        this.authService = authService;
    }

    // Permite al AuthService acceder a la misma lista
    public List<User> getUsersStorage() {
        return users;
    }

    @Override
    public IndividualCustomer registerIndividualCustomer(IndividualCustomer customer,
                                                         String username, String password) {
        // Validaciones de campos obligatorios
        validatePersonFields(customer.getName(), customer.getIdentificationId(),
                customer.getEmail(), customer.getPhone(), customer.getAddress());

        // Regla de negocio: mayoría de edad
        validateAge(customer.getBirthDate());

        // Regla de negocio: unicidad del número de identificación
        validateUniqueId(customer.getIdentificationId());

        // Validar email
        validateEmail(customer.getEmail());

        // Validar teléfono
        validatePhone(customer.getPhone());

        // Asignar rol fijo
        customer.setSystemRole(SystemRole.INDIVIDUAL_CUSTOMER);

        // Crear el usuario del sistema asociado
        User user = buildUser(customer, username, password);
        users.add(user);

        System.out.println("Cliente Persona Natural registrado: " + customer.getName());
        return customer;
    }

    @Override
    public CorporateCustomer registerCorporateCustomer(CorporateCustomer customer,
                                                        String username, String password) {
        // Validaciones
        if (customer.getBusinessName() == null || customer.getBusinessName().isBlank()) {
            throw new IllegalArgumentException("La Razón Social es obligatoria.");
        }
        validatePersonFields(customer.getBusinessName(), customer.getIdentificationId(),
                customer.getEmail(), customer.getPhone(), customer.getAddress());

        // Validar que el representante legal exista en el sistema
        if (customer.getLegalRepresentative() == null || customer.getLegalRepresentative().isBlank()) {
            throw new IllegalArgumentException("El Representante Legal es obligatorio.");
        }
        boolean repExists = users.stream()
                .anyMatch(u -> u.getIdentificationId().equals(customer.getLegalRepresentative()));
        if (!repExists) {
            throw new IllegalArgumentException(
                "El Representante Legal con ID '" + customer.getLegalRepresentative() + "' no existe en el sistema.");
        }

        // Unicidad de NIT
        validateUniqueId(customer.getIdentificationId());
        validateEmail(customer.getEmail());
        validatePhone(customer.getPhone());

        // Asignar rol fijo
        customer.setSystemRole(SystemRole.CORPORATE_CUSTOMER);

        User user = buildUser(customer, username, password);
        users.add(user);

        System.out.println("Cliente Empresa registrado: " + customer.getBusinessName());
        return customer;
    }

    @Override
    public User findByIdentificationId(String identificationId) {
        return users.stream()
                .filter(u -> u.getIdentificationId().equals(identificationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "No se encontró usuario con identificación: " + identificationId));
    }

    @Override
    public List<User> getAllUsers(User requestingUser) {
        // Solo el Analista Interno puede ver todos los usuarios
        authService.validateRole(requestingUser, SystemRole.INTERNAL_ANALYST);
        return new ArrayList<>(users);
    }

    @Override
    public void updateUserStatus(String identificationId, UserStatus newStatus, User requestingUser) {
        // Solo el Analista Interno puede cambiar estados
        authService.validateRole(requestingUser, SystemRole.INTERNAL_ANALYST);

        User target = findByIdentificationId(identificationId);
        target.setUserStatus(newStatus);
        System.out.println("Estado del usuario " + identificationId + " cambiado a: " + newStatus);
    }

    // ──────────────────────────────────────────────
    // Métodos privados de validación
    // ──────────────────────────────────────────────

    private void validatePersonFields(String name, String id, String email, String phone, String address) {
        if (name == null || name.isBlank())    throw new IllegalArgumentException("El nombre es obligatorio.");
        if (id == null || id.isBlank())        throw new IllegalArgumentException("El número de identificación es obligatorio.");
        if (email == null || email.isBlank())  throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        if (phone == null || phone.isBlank())  throw new IllegalArgumentException("El teléfono es obligatorio.");
        if (address == null || address.isBlank()) throw new IllegalArgumentException("La dirección es obligatoria.");
    }

    private void validateAge(LocalDate birthDate) {
        if (birthDate == null) throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 18) {
            throw new IllegalArgumentException("El cliente debe ser mayor de edad (mínimo 18 años).");
        }
    }

    private void validateUniqueId(String identificationId) {
        boolean exists = users.stream()
                .anyMatch(u -> u.getIdentificationId().equals(identificationId));
        if (exists) {
            throw new IllegalArgumentException(
                "Ya existe un usuario con el número de identificación: " + identificationId);
        }
    }

    private void validateEmail(String email) {
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido (debe contener '@' y un dominio).");
        }
    }

    private void validatePhone(String phone) {
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() < 7 || digits.length() > 15) {
            throw new IllegalArgumentException("El teléfono debe tener entre 7 y 15 dígitos.");
        }
    }

    private User buildUser(app.domain.models.Person person, String username, String password) {
        User user = new User();
        user.setName(person.getName());
        user.setIdentificationId(person.getIdentificationId());
        user.setEmail(person.getEmail());
        user.setPhone(person.getPhone());
        user.setBirthDate(person.getBirthDate());
        user.setAddress(person.getAddress());
        user.setSystemRole(person.getSystemRole());
        user.setUsername(username);
        user.setPassword(password); // En producción esto iría hasheado
        user.setUserStatus(UserStatus.ACTIVE);
        return user;
    }
}
