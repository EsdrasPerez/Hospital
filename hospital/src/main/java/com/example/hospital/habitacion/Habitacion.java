package com.example.hospital.habitacion;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "habitacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHabitacion;
    private String numero;
    private Integer piso;
    private Integer capacidad;
    private Long idTipoHabitacion;
    private Long idEstadoHabitacion;
    private Boolean activo = true;

}
