package app.application.adapters.api.response;

import app.domain.models.enums.AccountStatus;
import app.domain.models.enums.AccountType;
import app.domain.models.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountSummaryResponse {
    private String accountNumber;
    private AccountType accountType;
    private String accountHolderId;
    private double balance;
    private Currency currency;
    private AccountStatus accountStatus;
    private String openingDate;
}
