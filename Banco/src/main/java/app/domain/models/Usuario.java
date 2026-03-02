package app.domain.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Date;
import app.domain.enums.RolSistema;
import app.domain.enums.EstadoUsuario;

@Setter 
@Getter
@NoArgsConstructor

public abstract class Usuario {
    private String idUsuario; 
    private String idRelacionado;
    private String nombre;
    private String idIdentificacion; 
    private String correoElectronico; 
    private String telefono;
    private Date fechaNacimiento;
    private String direccion;
    private EstadoUsuario estadoUsuario; 
    private RolSistema rolSistema;
}
