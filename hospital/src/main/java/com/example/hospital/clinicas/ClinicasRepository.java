package com.example.hospital.clinicas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicasRepository extends JpaRepository<Clinicas, Long> {

    // Validar nombre único sin distinguir mayúsculas/minúsculas (RN-026)
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdClinicaNot(String nombre, Long idClinica);

    Optional<Clinicas> findByNombreIgnoreCase(String nombre);

    // Listar clínicas activas
    List<Clinicas> findByEstadoClinicaTrue();

    // Validar si un departamento tiene clínicas asignadas
    boolean existsByIdDepartamento(Long idDepartamento);

    // Validar si una especialidad tiene clínicas asociadas
    boolean existsByIdEspecialidad(Long idEspecialidad);
}
