package com.example.hospital.tratamiento;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "tratamiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTratamiento;

    private String descripcion;
    private Integer duracionDias;
    private String indicaciones;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long idDiagnostico;
    private Boolean activo = true;

}
