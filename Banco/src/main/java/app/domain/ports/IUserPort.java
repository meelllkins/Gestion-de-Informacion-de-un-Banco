package app.domain.ports;
 
import app.domain.enums.UserStatus;
import app.domain.models.User;
 
import java.util.List;
import java.util.Optional;
 
/**
 * Puerto de salida — Repositorio de Usuarios.
 * El dominio usa esta interfaz para persistir y consultar usuarios.
 * La implementación real (MySQL con JPA) va en la capa de infraestructura.
 */
public interface IUserPort {
 
    /** Guarda un nuevo usuario o actualiza uno existente. */
    void save(User user);
 
    /** Busca un usuario por número de identificación (DNI, Cédula, NIT). */
    Optional<User> findByIdentificationId(String identificationId);
 
    /** Busca un usuario por su nombre de usuario (para login). */
    Optional<User> findByUsername(String username);
 
    /** Retorna todos los usuarios del sistema. */
    List<User> findAll();
 
    /** Actualiza el estado de un usuario (ACTIVE, INACTIVE, BLOCKED). */
    void updateStatus(String identificationId, UserStatus newStatus);
 
    /** Verifica si ya existe un usuario con ese número de identificación. */
    boolean existsByIdentificationId(String identificationId);
}
