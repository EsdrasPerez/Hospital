package com.example.hospital.consultas;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "consultas")
@Data
public class Consultas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConsulta;
    private java.time.LocalDateTime fechaHora;
    private String sintomas;
    private java.math.BigDecimal peso;
    private java.math.BigDecimal talla;
    private java.math.BigDecimal temperatura;
    private String presionArterial;
    private Integer frecuenciaCardiaca;
    private Long idCita;
    private Boolean activo;

}
