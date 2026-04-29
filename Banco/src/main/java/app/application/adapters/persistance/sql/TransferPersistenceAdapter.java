package app.application.adapters.persistance.sql;

import app.application.adapters.persistance.sql.entities.TransferEntity;
import app.application.adapters.persistance.sql.repositories.TransferJpaRepository;
import app.domain.models.Transfer;
import app.domain.models.enums.TransferStatus;
import app.domain.ports.ITransferPort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class TransferPersistenceAdapter implements ITransferPort {

    private final TransferJpaRepository repository;

    public TransferPersistenceAdapter(TransferJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Transfer transfer) {
        TransferEntity entity = toEntity(transfer);
        repository.save(entity);
    }

    @Override
    public void saveAll(List<Transfer> transfers) {
        List<TransferEntity> entities = transfers.stream()
                .map(this::toEntity).toList();
        repository.saveAll(entities);
    }

    @Override
    public Optional<Transfer> findById(int transferId) {
        return repository.findByTransferId(transferId).map(this::toDomain);
    }

    @Override
    public List<Transfer> findByStatus(TransferStatus status) {
        return repository.findByTransferStatus(status).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<Transfer> findByAccount(String accountNumber) {
        return repository.findBySourceAccountOrDestinationAccount(accountNumber, accountNumber)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Transfer> findWaitingApprovalBefore(LocalDateTime dateTime) {
        return repository.findByTransferStatusAndCreationDateBefore(
                TransferStatus.PENDING, dateTime)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public void updateStatus(int transferId, TransferStatus newStatus,
                             LocalDateTime eventDate, Integer approverUserId) {
        repository.findByTransferId(transferId).ifPresent(entity -> {
            entity.setTransferStatus(newStatus);
            entity.setApprovalDate(eventDate);
            if (approverUserId != null) {
                entity.setApproverUserId(approverUserId);
            }
            repository.save(entity);
        });
    }

    private TransferEntity toEntity(Transfer transfer) {
        TransferEntity entity = new TransferEntity();
        entity.setTransferId(transfer.getTransferId());
        entity.setSourceAccount(transfer.getSourceAccount());
        entity.setDestinationAccount(transfer.getDestinationAccount());
        entity.setAmount(transfer.getAmount());
        entity.setCreationDate(transfer.getCreationDate());
        entity.setApprovalDate(transfer.getApprovalDate());
        entity.setTransferStatus(transfer.getTransferStatus());
        entity.setCreatorUserId(transfer.getCreatorUserId());
        entity.setApproverUserId(transfer.getApproverUserId());
        return entity;
    }

    private Transfer toDomain(TransferEntity entity) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(entity.getTransferId());
        transfer.setSourceAccount(entity.getSourceAccount());
        transfer.setDestinationAccount(entity.getDestinationAccount());
        transfer.setAmount(entity.getAmount());
        transfer.setCreationDate(entity.getCreationDate());
        transfer.setApprovalDate(entity.getApprovalDate());
        transfer.setTransferStatus(entity.getTransferStatus());
        transfer.setCreatorUserId(entity.getCreatorUserId());
        if (entity.getApproverUserId() != null) {
            transfer.setApproverUserId(entity.getApproverUserId());
        }
        return transfer;
    }
}