package com.example.hospital.departamentos;

import com.example.hospital.departamentos.dto.DepartamentosDTO;

import java.util.List;

public interface DepartamentosService {

    List<DepartamentosDTO> findAll();

    DepartamentosDTO findById(Long id);

    DepartamentosDTO crear(DepartamentosDTO dto);

    DepartamentosDTO actualizar(DepartamentosDTO dto, Long id);

    void eliminar(Long id);
}
