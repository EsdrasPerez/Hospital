package com.example.hospital.citas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitasRepository extends JpaRepository<Citas, Long> {

    // Validar conflicto de horario del médico (en creación)
    boolean existsByIdMedicoAndFechaAndHoraAndActivoTrue(Long idMedico, LocalDate fecha, LocalTime hora);

    // Validar conflicto de horario del médico (en actualización excluyendo la propia cita)
    boolean existsByIdMedicoAndFechaAndHoraAndActivoTrueAndIdCitaNot(Long idMedico, LocalDate fecha, LocalTime hora, Long idCita);

    // Listar citas activas
    List<Citas> findByActivoTrue();

    // Listar citas por paciente
    List<Citas> findByIdPaciente(Long idPaciente);

    boolean existsByIdPacienteAndActivoTrue(Long idPaciente);

    // Listar citas por médico
    List<Citas> findByIdMedico(Long idMedico);

    boolean existsByIdMedicoAndActivoTrue(Long idMedico);

    // Listar citas por fecha
    List<Citas> findByFecha(LocalDate fecha);
}
