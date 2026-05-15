package app.application.adapters.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BulkTransferRequest {

    @NotEmpty(message = "La lista de transferencias no puede estar vacía")
    @Valid
    private List<CreateTransferRequest> transfers;
}
