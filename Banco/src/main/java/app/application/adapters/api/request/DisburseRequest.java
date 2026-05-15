package app.application.adapters.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DisburseRequest {

    @NotBlank(message = "La cuenta destino de desembolso es obligatoria")
    private String destinationAccount;
}
