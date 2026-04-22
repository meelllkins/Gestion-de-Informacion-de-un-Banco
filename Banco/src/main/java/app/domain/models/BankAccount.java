package app.domain.models;

import app.domain.models.enums.AccountStatus;
import app.domain.models.enums.AccountType;
import app.domain.models.enums.Currency;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor

public class BankAccount extends GeneralBankProduct {
    private String accountNumber;
    private AccountType accountType;
    private String accountHolderId;
    private double balance;
    private Currency currency;
    private AccountStatus accountStatus;
    private String openingDate;
}