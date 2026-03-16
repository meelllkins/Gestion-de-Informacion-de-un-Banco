package app.domain.models;

import app.domain.enums.TransferStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor

public class Transfer {
    private int transferId;
    private String sourceAccount;
    private String destinationAccount;
    private double amount;
    private LocalDateTime creationDate; // Corregido: LocalDateTime para manejar vencimiento a 60 min
    private LocalDateTime approvalDate;
    private TransferStatus transferStatus;
    private int creatorUserId;
    private int approverUserId;
}