package com.example.hospital.tiposhabitacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TiposHabitacionRepository extends JpaRepository<TiposHabitacion, Long> {

    // Regla: descripcion única
    boolean existsByDescripcionIgnoreCase(String descripcion);

    boolean existsByDescripcionIgnoreCaseAndIdTipoHabitacionNot(String descripcion, Long idTipoHabitacion);

    Optional<TiposHabitacion> findByDescripcionIgnoreCase(String descripcion);

    List<TiposHabitacion> findByActivoTrue();
}
