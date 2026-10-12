package com.example.hospital.especialidadclinica;

import com.example.hospital.especialidadclinica.dto.EspecialidadClinicaDTO;

import java.util.List;

public interface EspecialidadClinicaService {

    List<EspecialidadClinicaDTO> findAll();

    List<EspecialidadClinicaDTO> findActivas();

    EspecialidadClinicaDTO findById(Long id);

    EspecialidadClinicaDTO crear(EspecialidadClinicaDTO dto);

    EspecialidadClinicaDTO actualizar(EspecialidadClinicaDTO dto, Long id);

    void eliminar(Long id);
}
