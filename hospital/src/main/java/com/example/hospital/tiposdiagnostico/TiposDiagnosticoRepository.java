package com.example.hospital.tiposdiagnostico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TiposDiagnosticoRepository extends JpaRepository<TiposDiagnostico, Long> {

    // Regla: descripcion única
    boolean existsByDescripcionIgnoreCase(String descripcion);

    boolean existsByDescripcionIgnoreCaseAndIdTipoDiagnosticoNot(String descripcion, Long idTipoDiagnostico);

    Optional<TiposDiagnostico> findByDescripcionIgnoreCase(String descripcion);

    List<TiposDiagnostico> findByActivoTrue();
}
