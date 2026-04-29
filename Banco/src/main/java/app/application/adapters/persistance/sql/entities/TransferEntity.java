package app.application.adapters.persistance.sql.entities;

import app.domain.models.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
public class TransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_id", nullable = false, unique = true)
    private int transferId;

    @Column(name = "source_account", nullable = false)
    private String sourceAccount;

    @Column(name = "destination_account", nullable = false)
    private String destinationAccount;

    @Column(nullable = false)
    private double amount;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_status", nullable = false)
    private TransferStatus transferStatus;

    @Column(name = "creator_user_id", nullable = false)
    private int creatorUserId;

    @Column(name = "approver_user_id")
    private Integer approverUserId;
}