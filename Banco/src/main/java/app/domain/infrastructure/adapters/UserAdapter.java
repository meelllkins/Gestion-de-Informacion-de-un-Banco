package app.domain.infrastructure.adapters;

import app.domain.enums.UserStatus;
import app.domain.models.User;
import app.domain.ports.IUserPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.domain.infrastructure.repositories.UserJpaRepository;

import java.util.List;
import java.util.Optional;

@Component
public class UserAdapter implements IUserPort {

    private final UserJpaRepository userJpaRepository;

    @Autowired
    public UserAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public void save(User user) {
        userJpaRepository.save(user);
    }

    @Override
    public Optional<User> findByIdentificationId(String identificationId) {
        return userJpaRepository.findByIdentificationId(identificationId);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll();
    }

    @Override
    public void updateStatus(String identificationId, UserStatus newStatus) {
        userJpaRepository.findByIdentificationId(identificationId).ifPresent(user -> {
            user.setUserStatus(newStatus);
            userJpaRepository.save(user);
        });
    }

    @Override
    public boolean existsByIdentificationId(String identificationId) {
        return userJpaRepository.existsByIdentificationId(identificationId);
    }
}