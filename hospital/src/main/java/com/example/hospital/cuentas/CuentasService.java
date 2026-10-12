package com.example.hospital.cuentas;

import com.example.hospital.cuentas.dto.CuentasDTO;

import java.math.BigDecimal;
import java.util.List;

public interface CuentasService {

    List<CuentasDTO> findAll();

    List<CuentasDTO> findActivas();

    List<CuentasDTO> findByPaciente(Long idPaciente);

    List<CuentasDTO> findByHospitalizacion(Long idHospitalizacion);

    CuentasDTO findById(Long id);

    CuentasDTO crear(CuentasDTO dto);

    CuentasDTO actualizar(CuentasDTO dto, Long id);

    CuentasDTO ajustarSaldo(Long id, BigDecimal montoAjuste);

    void eliminar(Long id);
}
