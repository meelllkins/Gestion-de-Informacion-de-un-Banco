package app.application.adapters.persistance.sql;

import app.application.adapters.persistance.sql.entities.UserEntity;
import app.application.adapters.persistance.sql.repositories.UserJpaRepository;
import app.domain.models.User;
import app.domain.models.enums.UserStatus;
import app.domain.ports.IUserPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserPersistenceAdapter implements IUserPort {

    private final UserJpaRepository repository;

    public UserPersistenceAdapter(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(User user) {
        UserEntity entity = toEntity(user);
        repository.save(entity);
    }

    @Override
    public Optional<User> findByIdentificationId(String identificationId) {
        return repository.findByIdentificationId(identificationId).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void updateStatus(String identificationId, UserStatus newStatus) {
        repository.findByIdentificationId(identificationId).ifPresent(entity -> {
            entity.setUserStatus(newStatus);
            repository.save(entity);
        });
    }

    @Override
    public boolean existsByIdentificationId(String identificationId) {
        return repository.existsByIdentificationId(identificationId);
    }

    private UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setName(user.getName());
        entity.setIdentificationId(user.getIdentificationId());
        entity.setEmail(user.getEmail());
        entity.setPhone(user.getPhone());
        entity.setBirthDate(user.getBirthDate());
        entity.setAddress(user.getAddress());
        entity.setSystemRole(user.getSystemRole());
        entity.setUsername(user.getUsername());
        entity.setPassword(user.getPassword());
        entity.setUserStatus(user.getUserStatus());
        entity.setRelatedId(user.getRelatedId());
        return entity;
    }

    private User toDomain(UserEntity entity) {
        User user = new User();
        user.setName(entity.getName());
        user.setIdentificationId(entity.getIdentificationId());
        user.setEmail(entity.getEmail());
        user.setPhone(entity.getPhone());
        user.setBirthDate(entity.getBirthDate());
        user.setAddress(entity.getAddress());
        user.setSystemRole(entity.getSystemRole());
        user.setUsername(entity.getUsername());
        user.setPassword(entity.getPassword());
        user.setUserStatus(entity.getUserStatus());
        user.setRelatedId(entity.getRelatedId());
        return user;
    }
}