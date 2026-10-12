package com.example.hospital.asignacionenfermeros;

import com.example.hospital.asignacionenfermeros.dto.AsignacionEnfermerosDTO;

import java.util.List;

public interface AsignacionEnfermerosService {
    List<AsignacionEnfermerosDTO> findAll();

    AsignacionEnfermerosDTO findById(Long id);

    AsignacionEnfermerosDTO crear(AsignacionEnfermerosDTO dto);

    AsignacionEnfermerosDTO actualizar(AsignacionEnfermerosDTO dto, Long id);

    void eliminar(Long id);

    List<AsignacionEnfermerosDTO> findByEnfermero(Long idEnfermero);

    List<AsignacionEnfermerosDTO> findByHospitalizacion(Long idHospitalizacion);
}
