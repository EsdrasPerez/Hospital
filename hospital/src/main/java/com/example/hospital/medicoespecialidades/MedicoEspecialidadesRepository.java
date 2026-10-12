package com.example.hospital.medicoespecialidades;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicoEspecialidadesRepository extends JpaRepository<MedicoEspecialidades, MedicoEspecialidades.MedicoEspecialidadesId> {

    // Validar si existe la asociación de especialidad y médico
    boolean existsByIdEspecialidadAndIdMedico(Long idEspecialidad, Long idMedico);

    // Listar especialidades asignadas a un médico
    List<MedicoEspecialidades> findByIdMedico(Long idMedico);

    boolean existsByIdMedico(Long idMedico);

    // Listar médicos que tienen una especialidad
    List<MedicoEspecialidades> findByIdEspecialidad(Long idEspecialidad);
}
