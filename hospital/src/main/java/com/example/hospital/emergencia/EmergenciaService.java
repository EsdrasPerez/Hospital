package com.example.hospital.emergencia;

import com.example.hospital.emergencia.dto.EmergenciaDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface EmergenciaService {

    List<EmergenciaDTO> findAll();

    List<EmergenciaDTO> findActivas();

    /** Filtros opcionales y combinables: idPaciente y soloActivas. */
    List<EmergenciaDTO> buscar(Long idPaciente, boolean soloActivas);

    EmergenciaDTO findById(Long id);

    List<EmergenciaDTO> findByPaciente(Long idPaciente);

    EmergenciaDTO crear(EmergenciaDTO dto);

    EmergenciaDTO actualizar(EmergenciaDTO dto, Long id);

    EmergenciaDTO darAlta(Long id, LocalDateTime fechaHoraAlta);

    void eliminar(Long id);
}
