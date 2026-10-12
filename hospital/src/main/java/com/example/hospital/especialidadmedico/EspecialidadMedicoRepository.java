package com.example.hospital.especialidadmedico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EspecialidadMedicoRepository extends JpaRepository<EspecialidadMedico, Long> {

    // Validar nombre único sin distinguir mayúsculas y minúsculas
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdEspecialidadNot(String nombre, Long idEspecialidad);

    Optional<EspecialidadMedico> findByNombreIgnoreCase(String nombre);

    // Listar especialidades activas
    List<EspecialidadMedico> findByActivoTrue();
}
