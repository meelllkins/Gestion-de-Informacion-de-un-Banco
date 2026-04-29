package app.application.adapters.persistance.sql.repositories;

import app.application.adapters.persistance.sql.entities.BankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountJpaRepository extends JpaRepository<BankAccountEntity, Long> {

    Optional<BankAccountEntity> findByAccountNumber(String accountNumber);

    List<BankAccountEntity> findByAccountHolderId(String holderId);

    boolean existsByAccountNumber(String accountNumber);
}