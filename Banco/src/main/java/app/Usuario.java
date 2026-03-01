package app;

import java.sql.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor

public class Usuario {
    private int Id_Usuario; 
    private String Id_Relacionado; 
    private String Nombre_Completo; 
    private String id_identificacion; 
    private String Correo_Electronico;
    private String Telefono; 
    private Date Fecha_Nacimiento;
    private String Direccion;
}
