package com.example.hospital.nivelesurgencia;

import com.example.hospital.nivelesurgencia.dto.NivelesUrgenciaDTO;

import java.util.List;

public interface NivelesUrgenciaService {
    List<NivelesUrgenciaDTO> findAll();

    NivelesUrgenciaDTO findById(Long id);

    NivelesUrgenciaDTO crear(NivelesUrgenciaDTO dto);

    NivelesUrgenciaDTO actualizar(NivelesUrgenciaDTO dto, Long id);

    void eliminar(Long id);
}
