package app.application.adapters.persistance.sql.repositories;

import app.application.adapters.persistance.sql.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByIdentificationId(String identificationId);

    Optional<UserEntity> findByUsername(String username);

    boolean existsByIdentificationId(String identificationId);
}