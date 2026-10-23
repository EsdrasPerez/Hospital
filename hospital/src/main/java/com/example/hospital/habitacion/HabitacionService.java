package com.example.hospital.habitacion;

import com.example.hospital.habitacion.dto.HabitacionDTO;

import java.util.List;

public interface HabitacionService {

    List<HabitacionDTO> findAll();

    List<HabitacionDTO> findActivas();

    /** Filtros opcionales y combinables. */
    List<HabitacionDTO> buscar(Integer piso, Long idTipoHabitacion, Long idEstadoHabitacion, boolean soloActivas, boolean soloDisponibles);

    HabitacionDTO findById(Long id);

    HabitacionDTO findByNumero(String numero);

    List<HabitacionDTO> findDisponibles();

    HabitacionDTO crear(HabitacionDTO dto);

    HabitacionDTO actualizar(HabitacionDTO dto, Long id);

    HabitacionDTO cambiarEstado(Long id, Long nuevoEstadoId);

    void eliminar(Long id);
}
