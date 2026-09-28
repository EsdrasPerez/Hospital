package com.example.hospital.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "paciente_alergias")
@Data
public class Paciente_alergias {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPacienteAlergia;
    @ManyToOne
    @JoinColumn(name = "id_paciente")
    private Paciente paciente;
    @ManyToOne
    @JoinColumn(name = "id_alergia")
    private Alergias alergia;
    private String observacion;

}
    
