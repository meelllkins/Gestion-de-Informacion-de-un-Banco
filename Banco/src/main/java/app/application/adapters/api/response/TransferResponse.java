package app.application.adapters.api.response;

import app.domain.models.enums.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TransferResponse {
    private int transferId;
    private String sourceAccount;
    private String destinationAccount;
    private double amount;
    private LocalDateTime creationDate;
    private LocalDateTime approvalDate;
    private TransferStatus transferStatus;
    private int creatorUserId;
}
