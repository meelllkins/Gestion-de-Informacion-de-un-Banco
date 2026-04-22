package app.domain.services.implementations;

import app.domain.models.User;
import app.domain.models.enums.SystemRole;
import app.domain.models.enums.UserStatus;
import app.domain.services.interfaces.IAuthService;

import java.util.Arrays;
import java.util.List;

public class AuthService implements IAuthService {

    // En un proyecto real esto sería un repositorio/BD.
    // Por ahora se inyecta desde afuera (lista compartida con UserService).
    private final List<User> users;

    public AuthService(List<User> users) {
        this.users = users;
    }

    @Override
    public User login(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }

        User user = users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas."));

        // Regla de negocio: usuarios inactivos o bloqueados no pueden ingresar
        if (user.getUserStatus() == UserStatus.INACTIVE) {
            throw new IllegalStateException("Usuario inactivo. Contacte al administrador.");
        }
        if (user.getUserStatus() == UserStatus.BLOCKED) {
            throw new IllegalStateException("Usuario bloqueado. Contacte al administrador.");
        }

        return user;
    }

    @Override
    public void validateRole(User user, SystemRole... allowedRoles) {
        if (user == null) {
            throw new SecurityException("Debe iniciar sesión para realizar esta operación.");
        }
        boolean hasPermission = Arrays.asList(allowedRoles).contains(user.getSystemRole());
        if (!hasPermission) {
            throw new SecurityException(
                "Acceso denegado. El rol '" + user.getSystemRole() + "' no tiene permiso para esta operación."
            );
        }
    }

    @Override
    public void logout(User user) {
        // En una app real se invalida el token de sesión.
        System.out.println("Sesión cerrada para el usuario: " + user.getUsername());
    }
}
