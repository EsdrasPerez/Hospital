package com.example.hospital.triaje;

import com.example.hospital.triaje.dto.TriajeDTO;

import java.util.List;

public interface TriajeService {
    List<TriajeDTO> findAll();

    TriajeDTO findById(Long id);

    TriajeDTO crear(TriajeDTO dto);

    TriajeDTO actualizar(TriajeDTO dto, Long id);

    void eliminar(Long id);
}
