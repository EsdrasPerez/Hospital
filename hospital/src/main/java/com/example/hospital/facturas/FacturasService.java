package com.example.hospital.facturas;

import com.example.hospital.facturas.dto.FacturasDTO;
import com.example.hospital.facturas.dto.SaldoFacturaDTO;

import java.util.List;

public interface FacturasService {

    List<FacturasDTO> findAll();

    List<FacturasDTO> findActivas();

    FacturasDTO findById(Long id);

    List<FacturasDTO> findByPaciente(Long idPaciente);

    List<FacturasDTO> findByHospitalizacion(Long idHospitalizacion);

    FacturasDTO crear(FacturasDTO dto);

    FacturasDTO actualizar(FacturasDTO dto, Long id);

    FacturasDTO cambiarEstado(Long id, Long nuevoEstadoId);

    void eliminar(Long id);

    SaldoFacturaDTO obtenerSaldo(Long idFactura);
}
