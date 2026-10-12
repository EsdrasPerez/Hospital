package com.example.hospital.pagos;

import com.example.hospital.pagos.dto.PagosDTO;

import java.util.List;

public interface PagosService {
    List<PagosDTO> findAll();

    PagosDTO findById(Long id);

    PagosDTO crear(PagosDTO dto);

    PagosDTO actualizar(PagosDTO dto, Long id);

    void eliminar(Long id);

    List<PagosDTO> findByFactura(Long idFactura);

    List<PagosDTO> findByCuenta(Long idCuenta);
}
