package com.example.hospital.emergencia;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emergencia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Emergencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEmergencia;
    private String nombreTemporal;
    private java.time.LocalDateTime fechaHoraIngreso;
    private String motivo;
    private String signosVitales;
    private java.time.LocalDateTime fechaHoraAlta;
    private Long idPaciente;
    private Long idHabitacion;
    private Long idMedico;
    private Long idTriaje;
    private Boolean activo;

}
