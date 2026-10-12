package com.example.hospital.especialidadmedico;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "especialidad_medico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EspecialidadMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEspecialidad;
    private String nombre;
    private String descripcion;
    private Boolean activo;

}
