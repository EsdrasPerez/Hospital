package com.example.hospital.especialidadmedico;

import com.example.hospital.especialidadmedico.dto.EspecialidadMedicoDTO;

import java.util.List;

public interface EspecialidadMedicoService {

    List<EspecialidadMedicoDTO> findAll();

    List<EspecialidadMedicoDTO> findActivas();

    EspecialidadMedicoDTO findById(Long id);

    EspecialidadMedicoDTO crear(EspecialidadMedicoDTO dto);

    EspecialidadMedicoDTO actualizar(EspecialidadMedicoDTO dto, Long id);

    void eliminar(Long id);
}
