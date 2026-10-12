package com.example.hospital.procedimientosemergencia;

import com.example.hospital.procedimientosemergencia.dto.ProcedimientosEmergenciaDTO;

import java.util.List;

public interface ProcedimientosEmergenciaService {
    List<ProcedimientosEmergenciaDTO> findAll();

    ProcedimientosEmergenciaDTO findById(Long id);

    ProcedimientosEmergenciaDTO crear(ProcedimientosEmergenciaDTO dto);

    ProcedimientosEmergenciaDTO actualizar(ProcedimientosEmergenciaDTO dto, Long id);

    void eliminar(Long id);
}
