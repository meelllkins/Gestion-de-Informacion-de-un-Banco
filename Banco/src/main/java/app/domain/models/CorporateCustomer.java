package app.domain.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor

public class CorporateCustomer extends Customer {
    private String businessName;
    private String legalRepresentative;
}

// Every user has a unique identifier, which can be a national ID or tax ID.