package app.application.adapters.persistance.sql;

import app.application.adapters.persistance.sql.entities.LoanEntity;
import app.application.adapters.persistance.sql.repositories.LoanJpaRepository;
import app.domain.models.Loan;
import app.domain.models.enums.LoanStatus;
import app.domain.ports.ILoanPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class LoanPersistenceAdapter implements ILoanPort {

    private final LoanJpaRepository repository;

    public LoanPersistenceAdapter(LoanJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Loan loan) {
        LoanEntity entity = toEntity(loan);
        repository.save(entity);
    }

    @Override
    public Optional<Loan> findById(int loanId) {
        return repository.findByLoanId(loanId).map(this::toDomain);
    }

    @Override
    public List<Loan> findByClientId(String clientId) {
        return repository.findByApplicantClientId(clientId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<Loan> findByStatus(LoanStatus status) {
        return repository.findByLoanStatus(status).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void updateStatus(int loanId, LoanStatus newStatus, LocalDate eventDate) {
        repository.findByLoanId(loanId).ifPresent(entity -> {
            entity.setLoanStatus(newStatus);
            if (newStatus == LoanStatus.APPROVED) {
                entity.setApprovalDate(eventDate);
            } else if (newStatus == LoanStatus.DISBURSED) {
                entity.setDisbursementDate(eventDate);
            }
            repository.save(entity);
        });
    }

    @Override
    public void updateApprovalData(int loanId, double approvedAmount, double interestRate) {
        repository.findByLoanId(loanId).ifPresent(entity -> {
            entity.setApprovedAmount(approvedAmount);
            entity.setInterestRate(interestRate);
            repository.save(entity);
        });
    }

    @Override
    public void updateDestinationAccount(int loanId, String destinationAccount) {
        repository.findByLoanId(loanId).ifPresent(entity -> {
            entity.setDestinationAccount(destinationAccount);
            repository.save(entity);
        });
    }

    private LoanEntity toEntity(Loan loan) {
        LoanEntity entity = new LoanEntity();
        entity.setProductCode(loan.getProductCode());
        entity.setProductName(loan.getProductName());
        entity.setCategory(loan.getCategory());
        entity.setRequiresApproval(loan.isRequiresApproval());
        entity.setLoanId(loan.getLoanId());
        entity.setLoanType(loan.getLoanType());
        entity.setApplicantClientId(loan.getApplicantClientId());
        entity.setRequestedAmount(loan.getRequestedAmount());
        entity.setApprovedAmount(loan.getApprovedAmount());
        entity.setInterestRate(loan.getInterestRate());
        entity.setTermMonths(loan.getTermMonths());
        entity.setApprovalDate(loan.getApprovalDate());
        entity.setDisbursementDate(loan.getDisbursementDate());
        entity.setDestinationAccount(loan.getDestinationAccount());
        entity.setLoanStatus(loan.getLoanStatus());
        return entity;
    }

    private Loan toDomain(LoanEntity entity) {
        Loan loan = new Loan();
        loan.setProductCode(entity.getProductCode());
        loan.setProductName(entity.getProductName());
        loan.setCategory(entity.getCategory());
        loan.setRequiresApproval(entity.isRequiresApproval());
        loan.setLoanId(entity.getLoanId());
        loan.setLoanType(entity.getLoanType());
        loan.setApplicantClientId(entity.getApplicantClientId());
        loan.setRequestedAmount(entity.getRequestedAmount());
        loan.setApprovedAmount(entity.getApprovedAmount());
        loan.setInterestRate(entity.getInterestRate());
        loan.setTermMonths(entity.getTermMonths());
        loan.setApprovalDate(entity.getApprovalDate());
        loan.setDisbursementDate(entity.getDisbursementDate());
        loan.setDestinationAccount(entity.getDestinationAccount());
        loan.setLoanStatus(entity.getLoanStatus());
        return loan;
    }
}