package com.example.hospital.diagnosticoconsulta;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Entity
@Table(name = "diagnostico_consulta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(DiagnosticoConsulta.DiagnosticoConsultaId.class)
public class DiagnosticoConsulta {

    @Id
    private Long idDiagnostico;

    @Id
    private Long idConsulta;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiagnosticoConsultaId implements Serializable {
        private Long idDiagnostico;
        private Long idConsulta;
    }
}
