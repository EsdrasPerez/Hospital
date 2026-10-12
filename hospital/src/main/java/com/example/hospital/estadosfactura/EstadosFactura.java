package com.example.hospital.estadosfactura;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estados_factura")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadosFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEstadoFactura;
    private String descripcion;
    private Boolean activo = true;

}
