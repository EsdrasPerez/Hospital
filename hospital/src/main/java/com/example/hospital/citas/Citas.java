package com.example.hospital.citas;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "citas")
@Data
public class Citas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCita;
    private java.time.LocalDate fecha;
    private java.time.LocalTime hora;
    private String motivo;
    private java.time.LocalDateTime fechaCreacion;
    private Long idEstado;
    private Long idMedico;
    private Long idPaciente;
    private Boolean activo;

}
