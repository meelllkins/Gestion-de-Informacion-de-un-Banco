package app.domain.models;

import app.domain.enums.TipoCuenta;
import app.domain.enums.EstadoCuenta;
import app.domain.enums.Moneda;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor

public class CuentaBancaria {
    private String numeroCuenta;
    private TipoCuenta tipoCuenta;
    private String idtitular;
    private long saldo;
    private Moneda moneda; 
    private EstadoCuenta estadoCuenta;
    private String fechaApertura;



}
