package com.example.hospital.detallefactura;

import com.example.hospital.detallefactura.dto.DetalleFacturaDTO;

import java.util.List;

public interface DetalleFacturaService {

    List<DetalleFacturaDTO> findAll();

    DetalleFacturaDTO findById(Long id);

    List<DetalleFacturaDTO> findByFactura(Long idFactura);

    DetalleFacturaDTO crear(DetalleFacturaDTO dto);

    DetalleFacturaDTO actualizar(DetalleFacturaDTO dto, Long id);

    void eliminar(Long id);
}
