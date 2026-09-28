package com.example.hospital.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "paciente")
@Data
public class Paciente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPaciente;

    private String numeroExpediente;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private LocalDate fechaNacimiento;
    private String sexo;
    private String telefono;
    private String email;
    private String direccion;
    @ManyToOne
    @JoinColumn(name = "id_tipo_sangre")
    private Tipo_sangre tipoSangre;
    private String contactoEmergencia;
    private String telefonoEmergencia;
    private LocalDateTime fechaRegistro;
}
