package com.example.hospital.estadosfactura;

import com.example.hospital.estadosfactura.dto.EstadosFacturaDTO;

import java.util.List;

public interface EstadosFacturaService {

    List<EstadosFacturaDTO> findAll();

    List<EstadosFacturaDTO> findActivos();

    EstadosFacturaDTO findById(Long id);

    EstadosFacturaDTO findByDescripcion(String descripcion);

    EstadosFacturaDTO crear(EstadosFacturaDTO dto);

    EstadosFacturaDTO actualizar(EstadosFacturaDTO dto, Long id);

    void eliminar(Long id);
}
