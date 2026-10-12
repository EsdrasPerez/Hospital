package com.example.hospital.tiposdiagnostico;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tipos_diagnostico")
@Data
public class TiposDiagnostico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipoDiagnostico;
    private String descripcion;
    private Boolean activo;

}
