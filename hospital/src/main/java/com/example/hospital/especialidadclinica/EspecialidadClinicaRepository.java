package com.example.hospital.especialidadclinica;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EspecialidadClinicaRepository extends JpaRepository<EspecialidadClinica, Long> {

    // Validar nombre único sin distinguir mayúsculas/minúsculas
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdEspecialidadNot(String nombre, Long idEspecialidad);

    Optional<EspecialidadClinica> findByNombreIgnoreCase(String nombre);

    // Listar especialidades clínicas activas
    List<EspecialidadClinica> findByActivoTrue();
}
