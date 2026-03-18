package app.domain.services.interfaces;

import app.domain.models.CorporateCustomer;
import app.domain.models.IndividualCustomer;
import app.domain.models.User;

import java.util.List;

/**
 * Servicio de gestión de usuarios y clientes.
 */
public interface IUserService {

    /**
     * Registra un nuevo cliente Persona Natural.
     * Valida: unicidad de identificación, mayoría de edad, campos obligatorios.
     */
    IndividualCustomer registerIndividualCustomer(IndividualCustomer customer, String username, String password);

    /**
     * Registra un nuevo cliente Empresa.
     * Valida: unicidad de NIT, representante legal existente, campos obligatorios.
     */
    CorporateCustomer registerCorporateCustomer(CorporateCustomer customer, String username, String password);

    /**
     * Busca un usuario por su número de identificación.
     */
    User findByIdentificationId(String identificationId);

    /**
     * Retorna todos los usuarios del sistema (solo para Analista Interno).
     */
    List<User> getAllUsers(User requestingUser);

    /**
     * Cambia el estado de un usuario (Activo, Inactivo, Bloqueado).
     * Solo roles administrativos internos pueden hacerlo.
     */
    void updateUserStatus(String identificationId, app.domain.enums.UserStatus newStatus, User requestingUser);
}
