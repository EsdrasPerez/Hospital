package com.example.hospital.medico;

import com.example.hospital.medico.dto.MedicoDTO;
import java.util.List;

public interface MedicoService {
    List<MedicoDTO> findAll();
    List<MedicoDTO> findActivos();
    MedicoDTO findById(Long id);
    MedicoDTO findByNoColegiado(String noColegiado);
    List<MedicoDTO> buscar(String criterio);
    /** Filtros combinables: `criterio` (nombre, apellidos o número de colegiado) y `soloActivos`. */
    List<MedicoDTO> buscar(String criterio, boolean soloActivos);
    MedicoDTO crear(MedicoDTO dto);
    MedicoDTO actualizar(MedicoDTO dto, Long id);
    MedicoDTO cambiarEstado(Long id, Boolean nuevoEstado);
    void eliminar(Long id);
}
