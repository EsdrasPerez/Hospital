package com.example.hospital.tratamientomedicamento;

import com.example.hospital.tratamientomedicamento.dto.TratamientoMedicamentoDTO;

import java.util.List;

public interface TratamientoMedicamentoService {
    List<TratamientoMedicamentoDTO> findAll();

    TratamientoMedicamentoDTO findById(Long idMedicamento, Long idTratamiento);

    TratamientoMedicamentoDTO crear(TratamientoMedicamentoDTO dto);

    void eliminar(Long idMedicamento, Long idTratamiento);
}
