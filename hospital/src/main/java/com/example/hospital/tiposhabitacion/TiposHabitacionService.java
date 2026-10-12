package com.example.hospital.tiposhabitacion;

import com.example.hospital.tiposhabitacion.dto.TiposHabitacionDTO;

import java.util.List;

public interface TiposHabitacionService {
    List<TiposHabitacionDTO> findAll();

    TiposHabitacionDTO findById(Long id);

    TiposHabitacionDTO crear(TiposHabitacionDTO dto);

    TiposHabitacionDTO actualizar(TiposHabitacionDTO dto, Long id);

    void eliminar(Long id);
}
