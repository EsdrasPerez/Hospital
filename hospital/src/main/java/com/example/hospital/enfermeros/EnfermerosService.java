package com.example.hospital.enfermeros;

import com.example.hospital.enfermeros.dto.EnfermerosDTO;

import java.util.List;

public interface EnfermerosService {

    List<EnfermerosDTO> findAll();

    List<EnfermerosDTO> findActivos();

    EnfermerosDTO findById(Long id);

    EnfermerosDTO crear(EnfermerosDTO dto);

    EnfermerosDTO actualizar(EnfermerosDTO dto, Long id);

    void eliminar(Long id);
}
