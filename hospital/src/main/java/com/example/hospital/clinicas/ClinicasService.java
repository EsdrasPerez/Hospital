package com.example.hospital.clinicas;

import com.example.hospital.clinicas.dto.ClinicasDTO;

import java.util.List;

public interface ClinicasService {

    List<ClinicasDTO> findAll();

    List<ClinicasDTO> findActivas();

    ClinicasDTO findById(Long id);

    ClinicasDTO crear(ClinicasDTO dto);

    ClinicasDTO actualizar(ClinicasDTO dto, Long id);

    void eliminar(Long id);

    // RN-027: Método de utilidad para validar si una clínica está disponible para atenciones
    void validarClinicaActivaParaAtencion(Long idClinica);
}
