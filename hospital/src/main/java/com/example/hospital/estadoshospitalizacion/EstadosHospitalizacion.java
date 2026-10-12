package com.example.hospital.estadoshospitalizacion;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estados_hospitalizacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadosHospitalizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEstadoHosp;
    private String descripcion;
    private Boolean activo = true;

}
