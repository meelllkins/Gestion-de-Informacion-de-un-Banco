package app.domain.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor

public class ClienteEmpresa extends Usuario{
    private String razonSocial;
    private String representanteLegal;
}

//Todo usuario tiene un identificador único, que puede ser cédula o NIT.