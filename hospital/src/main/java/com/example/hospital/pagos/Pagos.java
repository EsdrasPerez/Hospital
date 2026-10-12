package com.example.hospital.pagos;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pagos")
@Data
public class Pagos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPago;
    private Long idFactura;
    private Long idCuenta;
    private Long idMetodoPago;
    private java.math.BigDecimal monto;
    private java.time.LocalDateTime fechaPago;
    private String referencia;
    private Boolean activo;

}
