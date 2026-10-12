package com.example.hospital.cuentas;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cuentas")
@Data
public class Cuentas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCuenta;
    private Long idPaciente;
    private Long idHospitalizacion;
    private java.time.LocalDateTime fechaApertura;
    private String estado;
    private java.math.BigDecimal saldo;
    private Boolean activo;

}
