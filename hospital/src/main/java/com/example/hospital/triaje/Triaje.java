package com.example.hospital.triaje;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "triaje")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Triaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTriaje;
    private String nivelDeEmergencia;
    private String descripcion;
    private Boolean activo;

}
