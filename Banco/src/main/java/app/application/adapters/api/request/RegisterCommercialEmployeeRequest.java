package app.application.adapters.api.request;

import app.domain.models.GeneralBankProduct;
import app.domain.models.enums.SystemRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RegisterCommercialEmployeeRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El número de identificación es obligatorio")
    private String identificationId;

    @NotBlank(message = "El correo electrónico es obligatorio")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    private String phone;

    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate birthDate;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotNull(message = "El rol del sistema es obligatorio")
    private SystemRole systemRole;

    private List<GeneralBankProduct> generalBankProduct;
}