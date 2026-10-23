package com.example.hospital.pagos;

import com.example.hospital.pagos.dto.PagosDTO;

import java.util.List;

public interface PagosService {
    List<PagosDTO> findAll();
    PagosDTO findById(Long id);
    PagosDTO crear(PagosDTO dto);
    /** Solo corrige método de pago y referencia (ver reglas en el controlador). */
    PagosDTO actualizar(PagosDTO dto, Long id);
    /** Anula el pago (borrado lógico). */
    void eliminar(Long id);
    List<PagosDTO> findByFactura(Long idFactura, boolean soloActivos);
    List<PagosDTO> findByCuenta(Long idCuenta, boolean soloActivos);
}
