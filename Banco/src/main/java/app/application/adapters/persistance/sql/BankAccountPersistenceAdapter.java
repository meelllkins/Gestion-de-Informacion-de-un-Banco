package app.application.adapters.persistance.sql;

import app.application.adapters.persistance.sql.entities.BankAccountEntity;
import app.application.adapters.persistance.sql.repositories.BankAccountJpaRepository;
import app.domain.models.BankAccount;
import app.domain.models.enums.AccountStatus;
import app.domain.ports.IAccountPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BankAccountPersistenceAdapter implements IAccountPort {

    private final BankAccountJpaRepository repository;

    public BankAccountPersistenceAdapter(BankAccountJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(BankAccount account) {
        BankAccountEntity entity = toEntity(account);
        repository.save(entity);
    }

    @Override
    public Optional<BankAccount> findByAccountNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber).map(this::toDomain);
    }

    @Override
    public List<BankAccount> findByHolderId(String holderId) {
        return repository.findByAccountHolderId(holderId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void updateBalance(String accountNumber, double newBalance) {
        repository.findByAccountNumber(accountNumber).ifPresent(entity -> {
            entity.setBalance(newBalance);
            repository.save(entity);
        });
    }

    @Override
    public void updateStatus(String accountNumber, AccountStatus newStatus) {
        repository.findByAccountNumber(accountNumber).ifPresent(entity -> {
            entity.setAccountStatus(newStatus);
            repository.save(entity);
        });
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return repository.existsByAccountNumber(accountNumber);
    }

    private BankAccountEntity toEntity(BankAccount account) {
        BankAccountEntity entity = new BankAccountEntity();
        entity.setProductCode(account.getProductCode());
        entity.setProductName(account.getProductName());
        entity.setCategory(account.getCategory());
        entity.setRequiresApproval(account.isRequiresApproval());
        entity.setAccountNumber(account.getAccountNumber());
        entity.setAccountType(account.getAccountType());
        entity.setAccountHolderId(account.getAccountHolderId());
        entity.setBalance(account.getBalance());
        entity.setCurrency(account.getCurrency());
        entity.setAccountStatus(account.getAccountStatus());
        entity.setOpeningDate(account.getOpeningDate());
        return entity;
    }

    private BankAccount toDomain(BankAccountEntity entity) {
        BankAccount account = new BankAccount();
        account.setProductCode(entity.getProductCode());
        account.setProductName(entity.getProductName());
        account.setCategory(entity.getCategory());
        account.setRequiresApproval(entity.isRequiresApproval());
        account.setAccountNumber(entity.getAccountNumber());
        account.setAccountType(entity.getAccountType());
        account.setAccountHolderId(entity.getAccountHolderId());
        account.setBalance(entity.getBalance());
        account.setCurrency(entity.getCurrency());
        account.setAccountStatus(entity.getAccountStatus());
        account.setOpeningDate(entity.getOpeningDate());
        return account;
    }
}