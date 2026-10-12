package com.example.hospital.tiposdiagnostico;

import com.example.hospital.tiposdiagnostico.dto.TiposDiagnosticoDTO;

import java.util.List;

public interface TiposDiagnosticoService {
    List<TiposDiagnosticoDTO> findAll();

    TiposDiagnosticoDTO findById(Long id);

    TiposDiagnosticoDTO crear(TiposDiagnosticoDTO dto);

    TiposDiagnosticoDTO actualizar(TiposDiagnosticoDTO dto, Long id);

    void eliminar(Long id);
}
