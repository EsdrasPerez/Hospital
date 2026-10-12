package com.example.hospital.nivelesurgencia;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "niveles_urgencia")
@Data
public class NivelesUrgencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNivelUrgencia;
    private String codigo;
    private String descripcion;
    private Integer tiempoMaxEsperaMin;
    private String color;
    private Boolean activo;

}
