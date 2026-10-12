package com.example.hospital.enfermeros;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnfermerosRepository extends JpaRepository<Enfermeros, Long> {

    // Validar cédula profesional única
    boolean existsByCedulaProfesional(String cedulaProfesional);

    boolean existsByCedulaProfesionalAndIdEnfermeroNot(String cedulaProfesional, Long idEnfermero);

    Optional<Enfermeros> findByCedulaProfesional(String cedulaProfesional);

    List<Enfermeros> findByActivoTrue();

    // Validar si existen enfermeros asignados a un departamento
    boolean existsByIdDepartamento(Long idDepartamento);
}
