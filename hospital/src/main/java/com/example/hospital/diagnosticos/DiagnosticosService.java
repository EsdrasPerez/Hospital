package com.example.hospital.diagnosticos;

import com.example.hospital.diagnosticos.dto.DiagnosticosDTO;

import java.time.LocalDate;
import java.util.List;

public interface DiagnosticosService {

    List<DiagnosticosDTO> findAll();

    List<DiagnosticosDTO> findActivos();

    List<DiagnosticosDTO> findByTipo(Long idTipoDiagnostico);

    List<DiagnosticosDTO> findByFecha(LocalDate fecha);

    DiagnosticosDTO findById(Long id);

    DiagnosticosDTO crear(DiagnosticosDTO dto);

    DiagnosticosDTO actualizar(DiagnosticosDTO dto, Long id);

    void eliminar(Long id);
}
