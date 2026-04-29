package app.application.adapters.persistance.sql.repositories;

import app.application.adapters.persistance.sql.entities.TransferEntity;
import app.domain.models.enums.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransferJpaRepository extends JpaRepository<TransferEntity, Long> {

    Optional<TransferEntity> findByTransferId(int transferId);

    List<TransferEntity> findByTransferStatus(TransferStatus status);

    List<TransferEntity> findBySourceAccountOrDestinationAccount(
            String sourceAccount, String destinationAccount);

    List<TransferEntity> findByTransferStatusAndCreationDateBefore(
            TransferStatus status, LocalDateTime dateTime);
}