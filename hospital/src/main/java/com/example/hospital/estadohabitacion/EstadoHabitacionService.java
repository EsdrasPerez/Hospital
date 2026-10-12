package com.example.hospital.estadohabitacion;

import com.example.hospital.estadohabitacion.dto.EstadoHabitacionDTO;

import java.util.List;

public interface EstadoHabitacionService {
    List<EstadoHabitacionDTO> findAll();

    EstadoHabitacionDTO findById(Long id);

    EstadoHabitacionDTO crear(EstadoHabitacionDTO dto);

    EstadoHabitacionDTO actualizar(EstadoHabitacionDTO dto, Long id);

    void eliminar(Long id);
}
