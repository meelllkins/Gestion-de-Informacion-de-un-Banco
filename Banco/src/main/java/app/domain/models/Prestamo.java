package app.domain.models;

import java.time.LocalDate;

import app.domain.enums.EstadoPrestamo;
import app.domain.enums.TipoPrestamo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class Prestamo {
    private int idPrestamo; 
    private TipoPrestamo tipoPrestamo;
    private String idClienteSolicitante;
    private long montoSolicitado;
    private long montoAprobado;
    private long tasaInteres; 
    private int plazoMeses; 
    private LocalDate fechaAprobacion; 
    private LocalDate fechaDesembolso; 
    private String CuentaDestino;
    private EstadoPrestamo estadoPrestamo;


}
