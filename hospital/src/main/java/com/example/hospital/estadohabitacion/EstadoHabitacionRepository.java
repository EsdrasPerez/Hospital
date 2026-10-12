package com.example.hospital.estadohabitacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoHabitacionRepository extends JpaRepository<EstadoHabitacion, Long> {

    // Regla: descripción única sin distinguir mayúsculas/minúsculas
    boolean existsByDescripcionIgnoreCase(String descripcion);

    boolean existsByDescripcionIgnoreCaseAndIdEstadoHabitacionNot(String descripcion, Long idEstadoHabitacion);

    Optional<EstadoHabitacion> findByDescripcionIgnoreCase(String descripcion);

    List<EstadoHabitacion> findByActivoTrue();
}
