package app.application.adapters.api.response;

import app.domain.models.enums.SystemRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterCorporateCustomerResponse {

    private String businessName;
    private String identificationId;
    private String email;
    private String phone;
    private String address;
    private String legalRepresentative;
    private SystemRole systemRole;
    private String username;
}