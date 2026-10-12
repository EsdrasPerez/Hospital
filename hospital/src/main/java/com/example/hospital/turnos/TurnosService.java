package com.example.hospital.turnos;

import com.example.hospital.turnos.dto.TurnosDTO;

import java.util.List;

public interface TurnosService {
    List<TurnosDTO> findAll();

    TurnosDTO findById(Long id);

    TurnosDTO registrar(TurnosDTO dto);

    TurnosDTO actualizar(TurnosDTO dto, Long id);

    void eliminar(Long id);
}
