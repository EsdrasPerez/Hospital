package com.example.hospital.tiposhabitacion;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "tipos_habitacion")
@Data
public class TiposHabitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipoHabitacion;
    private String descripcion;
    private BigDecimal costoDia;
    private Boolean activo;

}
