package com.example.hospital.tratamiento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TratamientoRepository extends JpaRepository<Tratamiento, Long> {

    // Validar si un diagnóstico tiene tratamientos asociados
    boolean existsByIdDiagnostico(Long idDiagnostico);

    // Listar tratamientos por diagnóstico
    List<Tratamiento> findByIdDiagnostico(Long idDiagnostico);

    // Listar tratamientos activos
    List<Tratamiento> findByActivoTrue();

    // Listar tratamientos activos por diagnóstico
    List<Tratamiento> findByIdDiagnosticoAndActivoTrue(Long idDiagnostico);
}
