package app.application.adapters.persistance.sql.repositories;

import app.application.adapters.persistance.sql.entities.LoanEntity;
import app.domain.models.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanJpaRepository extends JpaRepository<LoanEntity, Long> {

    Optional<LoanEntity> findByLoanId(int loanId);

    List<LoanEntity> findByApplicantClientId(String clientId);

    List<LoanEntity> findByLoanStatus(LoanStatus status);
}