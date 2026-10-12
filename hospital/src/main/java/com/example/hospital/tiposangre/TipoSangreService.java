package com.example.hospital.tiposangre;

import com.example.hospital.tiposangre.dto.TipoSangreDTO;

import java.util.List;

public interface TipoSangreService {
    List<TipoSangreDTO> findAll();

    TipoSangreDTO findById(Long id);

    TipoSangreDTO crear(TipoSangreDTO dto);

    TipoSangreDTO actualizar(TipoSangreDTO dto, Long id);

    void eliminar(Long id);
}
