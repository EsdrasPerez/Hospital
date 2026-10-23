package com.example.hospital.asignacionenfermeros;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AsignacionEnfermerosRepository extends JpaRepository<AsignacionEnfermeros, Long> {

    // Regla 7: No debe existir una asignación idéntica para el mismo enfermero, hospitalización, fecha y turno
    boolean existsByIdEnfermeroAndIdHospitalizacionAndFechaAsignacionAndIdTurno(
            Long idEnfermero, Long idHospitalizacion, LocalDate fechaAsignacion, Long idTurno
    );

    // Para evitar duplicado al actualizar excluyendo la misma asignación
    boolean existsByIdEnfermeroAndIdHospitalizacionAndFechaAsignacionAndIdTurnoAndIdAsignacionNot(
            Long idEnfermero, Long idHospitalizacion, LocalDate fechaAsignacion, Long idTurno, Long idAsignacion
    );

    List<AsignacionEnfermeros> findByIdEnfermero(Long idEnfermero);

    // Verificar si un enfermero tiene asignaciones registradas
    boolean existsByIdEnfermero(Long idEnfermero);

    boolean existsByIdEnfermeroAndActivoTrue(Long idEnfermero);

    List<AsignacionEnfermeros> findByIdHospitalizacion(Long idHospitalizacion);

    // RN-13 / RN-14 (Turnos): Verificar si un turno está siendo utilizado en asignaciones
    boolean existsByIdTurno(Long idTurno);

    // RN-11 (Turnos): Verificar si un turno tiene asignaciones vigentes o futuras
    boolean existsByIdTurnoAndFechaAsignacionGreaterThanEqual(Long idTurno, LocalDate fecha);
}
