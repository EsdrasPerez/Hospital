package com.example.hospital.diagnosticoconsulta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosticoConsultaRepository extends JpaRepository<DiagnosticoConsulta, DiagnosticoConsulta.DiagnosticoConsultaId> {

    // Validar si un diagnóstico ya fue asociado a una consulta
    boolean existsByIdDiagnosticoAndIdConsulta(Long idDiagnostico, Long idConsulta);

    // Listar todos los diagnósticos de una consulta específica
    List<DiagnosticoConsulta> findByIdConsulta(Long idConsulta);

    // Validar si existen diagnósticos vinculados a una consulta
    boolean existsByIdConsulta(Long idConsulta);

    // Listar todas las consultas donde se emitió un diagnóstico
    List<DiagnosticoConsulta> findByIdDiagnostico(Long idDiagnostico);
}
