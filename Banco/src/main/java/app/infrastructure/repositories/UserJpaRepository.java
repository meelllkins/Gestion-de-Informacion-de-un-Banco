package app.infrastructure.repositories;

import app.domain.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, String> {

    Optional<User> findByIdentificationId(String identificationId);

    Optional<User> findByUsername(String username);

    boolean existsByIdentificationId(String identificationId);
}