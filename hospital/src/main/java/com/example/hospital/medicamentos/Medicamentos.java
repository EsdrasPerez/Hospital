package com.example.hospital.medicamentos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "medicamentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medicamentos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMedicamento;
    private String nombreComercial;
    private String nombreGenerico;
    private String presentacion;
    private String laboratorio;
    private Integer stock;
    private Integer stockMinimo;
    private BigDecimal precio;
    private Boolean requiereReceta;
    private Boolean activo = true;

}
