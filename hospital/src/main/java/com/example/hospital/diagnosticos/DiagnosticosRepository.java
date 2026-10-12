package com.example.hospital.diagnosticos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiagnosticosRepository extends JpaRepository<Diagnosticos, Long> {

    // Integridad referencial con tipos de diagnóstico
    boolean existsByIdTipoDiagnostico(Long idTipoDiagnostico);

    // Listar diagnósticos activos
    List<Diagnosticos> findByActivoTrue();

    // Listar diagnósticos por tipo
    List<Diagnosticos> findByIdTipoDiagnostico(Long idTipoDiagnostico);

    // Listar diagnósticos por fecha
    List<Diagnosticos> findByFecha(LocalDate fecha);
}
