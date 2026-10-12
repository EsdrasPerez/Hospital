package com.example.hospital.departamentos;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "departamentos")
@Data
public class Departamentos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDepartamento;
    private String nombre;
    private String descripcion;
    private String telefono;
    private Boolean estado;

}
