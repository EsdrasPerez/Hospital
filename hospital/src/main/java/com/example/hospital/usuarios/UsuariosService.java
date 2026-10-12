package com.example.hospital.usuarios;

import com.example.hospital.usuarios.dto.UsuariosDTO;

import java.util.List;

public interface UsuariosService {
    List<UsuariosDTO> findAll();

    UsuariosDTO findById(Long id);

    UsuariosDTO crear(UsuariosDTO dto);

    UsuariosDTO actualizar(UsuariosDTO dto, Long id);

    void eliminar(Long id);
}
