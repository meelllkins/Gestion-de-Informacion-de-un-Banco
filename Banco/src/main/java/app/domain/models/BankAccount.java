package app.domain.models;

import app.domain.enums.AccountType;
import app.domain.enums.AccountStatus;
import app.domain.enums.Currency;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor

public class BankAccount {
    private String accountNumber;
    private AccountType accountType;
    private String accountHolderId;
    private double balance;
    private Currency currency;
    private AccountStatus accountStatus;
    private String openingDate;
}