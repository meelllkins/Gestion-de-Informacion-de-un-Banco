package app.domain.models;

import app.domain.enums.TransferStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor

public class Transfer {
    private int transferId;
    private String sourceAccount;
    private String destinationAccount;
    private double amount;
    private LocalDate creationDate;
    private LocalDate approvalDate;
    private TransferStatus transferStatus;
    private int creatorUserId;
    private int approverUserId;
}