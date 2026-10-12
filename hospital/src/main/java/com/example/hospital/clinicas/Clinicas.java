package com.example.hospital.clinicas;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "clinicas")
@Data
public class Clinicas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idClinica;
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private String telefono;
    private Boolean estadoClinica;
    private Long idDepartamento;
    private Long idEspecialidad;

}
