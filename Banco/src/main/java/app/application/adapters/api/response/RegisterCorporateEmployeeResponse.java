package app.application.adapters.api.response;

import app.domain.models.enums.SystemRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class RegisterCorporateEmployeeResponse {

    private String name;
    private String identificationId;
    private String email;
    private String phone;
    private String address;
    private LocalDate birthDate;
    private String relatedId;
    private SystemRole systemRole;
    private String username;
}
