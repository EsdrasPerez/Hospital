package com.example.hospital.medicoespecialidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Entity
@Table(name = "medico_especialidades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(MedicoEspecialidades.MedicoEspecialidadesId.class)
public class MedicoEspecialidades {

    @Id
    private Long idEspecialidad;

    @Id
    private Long idMedico;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicoEspecialidadesId implements Serializable {
        private Long idEspecialidad;
        private Long idMedico;
    }
}
