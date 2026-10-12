package com.example.hospital.especialidadclinica;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "especialidad_clinica")
@Data
public class EspecialidadClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEspecialidad;
    private String nombre;
    private String descripcion;
    private Boolean activo;

}
