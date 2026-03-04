package app.domain.models;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor

public abstract class Customer extends Person {
    private List<GeneralBankProduct> GeneralBankProduct;

}