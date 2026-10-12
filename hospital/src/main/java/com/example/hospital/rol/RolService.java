package com.example.hospital.rol;

import com.example.hospital.rol.dto.RolDTO;

import java.util.List;

public interface RolService {
    List<RolDTO> findAll();

    RolDTO findById(Long id);

    RolDTO crear(RolDTO dto);

    RolDTO actualizar(RolDTO dto, Long id);

    void eliminar(Long id);
}
