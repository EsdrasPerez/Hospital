package com.example.hospital.estadocita;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoCitaRepository extends JpaRepository<EstadoCita, Long> {

    // RN-EC-01: descripcion única
    boolean existsByDescripcionIgnoreCase(String descripcion);

    boolean existsByDescripcionIgnoreCaseAndIdEstadoNot(String descripcion, Long idEstado);

    Optional<EstadoCita> findByDescripcionIgnoreCase(String descripcion);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdEstadoNot(String nombre, Long idEstado);

    List<EstadoCita> findByActivoTrue();
}
