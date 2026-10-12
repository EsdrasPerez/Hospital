package com.example.hospital.habitacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    List<Habitacion> findByActivoTrue();

    Optional<Habitacion> findByNumero(String numero);

    boolean existsByNumero(String numero);

    boolean existsByNumeroAndIdHabitacionNot(String numero, Long idHabitacion);

    List<Habitacion> findByIdEstadoHabitacionAndActivoTrue(Long idEstadoHabitacion);

    List<Habitacion> findByIdTipoHabitacionAndActivoTrue(Long idTipoHabitacion);

    // Regla de integridad: verificar si existen habitaciones asignadas a un estado
    boolean existsByIdEstadoHabitacion(Long idEstadoHabitacion);

    // Regla de integridad: verificar si existen habitaciones asignadas a un tipo de habitacion
    boolean existsByIdTipoHabitacion(Long idTipoHabitacion);

    long countByIdEstadoHabitacionAndActivoTrue(Long idEstadoHabitacion);
}
