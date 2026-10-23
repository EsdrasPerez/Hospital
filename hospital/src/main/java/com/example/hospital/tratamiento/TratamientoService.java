package com.example.hospital.tratamiento;

import com.example.hospital.tratamiento.dto.TratamientoDTO;

import java.util.List;

public interface TratamientoService {

    List<TratamientoDTO> findAll();

    List<TratamientoDTO> findActivos();

    TratamientoDTO findById(Long id);

    /** Filtros opcionales y combinables; sin soloActivos devuelve también los inactivos (igual que el listado general). */
    List<TratamientoDTO> buscar(Long idDiagnostico, boolean soloActivos);

    List<TratamientoDTO> findByDiagnostico(Long idDiagnostico);

    TratamientoDTO crear(TratamientoDTO dto);

    TratamientoDTO actualizar(TratamientoDTO dto, Long id);

    void eliminar(Long id);
}
