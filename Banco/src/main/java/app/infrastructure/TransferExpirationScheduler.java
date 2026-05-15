package app.infrastructure;

import app.domain.services.interfaces.ITransferService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TransferExpirationScheduler {

    private final ITransferService transferService;

    public TransferExpirationScheduler(ITransferService transferService) {
        this.transferService = transferService;
    }

    @Scheduled(fixedRate = 300_000)
    public void expireStaleTransfers() {
        transferService.checkAndExpireTransfers();
    }
}
