package com.example.hospital.diagnosticoconsulta;

import com.example.hospital.diagnosticoconsulta.dto.DiagnosticoConsultaDTO;

import java.util.List;

public interface DiagnosticoConsultaService {

    List<DiagnosticoConsultaDTO> findAll();

    List<DiagnosticoConsultaDTO> findByConsulta(Long idConsulta);

    List<DiagnosticoConsultaDTO> findByDiagnostico(Long idDiagnostico);

    DiagnosticoConsultaDTO findById(Long idDiagnostico, Long idConsulta);

    DiagnosticoConsultaDTO crear(DiagnosticoConsultaDTO dto);

    void eliminar(Long idDiagnostico, Long idConsulta);
}
