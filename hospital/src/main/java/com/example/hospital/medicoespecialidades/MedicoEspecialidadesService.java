package com.example.hospital.medicoespecialidades;

import com.example.hospital.medicoespecialidades.dto.MedicoEspecialidadesDTO;

import java.util.List;

public interface MedicoEspecialidadesService {

    List<MedicoEspecialidadesDTO> findAll();

    List<MedicoEspecialidadesDTO> findByMedico(Long idMedico);

    List<MedicoEspecialidadesDTO> findByEspecialidad(Long idEspecialidad);

    MedicoEspecialidadesDTO findById(Long idEspecialidad, Long idMedico);

    MedicoEspecialidadesDTO crear(MedicoEspecialidadesDTO dto);

    void eliminar(Long idEspecialidad, Long idMedico);
}
