package com.example.hospital.enfermeros;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "enfermeros")
@Data
public class Enfermeros {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEnfermero;
    private String cedulaProfesional;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String telefono;
    private Long idDepartamento;
    private Boolean activo;

}
