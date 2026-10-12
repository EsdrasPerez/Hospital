package com.example.hospital.detallefactura;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "detalle_factura")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalle;
    private Long idFactura;
    private String concepto;
    private Integer cantidad;
    private java.math.BigDecimal precioUnitario;
    private java.math.BigDecimal importe;

}
