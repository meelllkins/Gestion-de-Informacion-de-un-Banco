package app.domain.services.interfaces;

import app.domain.models.User;

/**
 * Servicio de autenticación.
 * Todo usuario debe estar logueado para realizar cualquier operación.
 */
public interface IAuthService {

    /**
     * Autentica a un usuario con sus credenciales.
     * @return El usuario autenticado, o lanza excepción si las credenciales son inválidas.
     */
    User login(String username, String password);

    /**
     * Valida que el usuario tenga el rol requerido para ejecutar una operación.
     * Si no tiene permiso, lanza una excepción de acceso denegado.
     */
    void validateRole(User user, app.domain.models.enums.SystemRole... allowedRoles);

    /**
     * Cierra la sesión del usuario actual.
     */
    void logout(User user);
}
