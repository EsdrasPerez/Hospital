package com.example.hospital.metodospago;

import com.example.hospital.metodospago.dto.MetodosPagoDTO;

import java.util.List;

public interface MetodosPagoService {
    List<MetodosPagoDTO> findAll();

    MetodosPagoDTO findById(Long id);

    MetodosPagoDTO crear(MetodosPagoDTO dto);

    MetodosPagoDTO actualizar(MetodosPagoDTO dto, Long id);

    void eliminar(Long id);
}
