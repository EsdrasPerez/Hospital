package com.example.hospital.emergencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergenciaRepository extends JpaRepository<Emergencia, Long> {

    List<Emergencia> findByActivoTrue();

    List<Emergencia> findByIdPaciente(Long idPaciente);

    List<Emergencia> findByIdPacienteAndActivoTrue(Long idPaciente);

    List<Emergencia> findByIdMedicoAndActivoTrue(Long idMedico);

    List<Emergencia> findByIdHabitacionAndActivoTrue(Long idHabitacion);

    List<Emergencia> findByIdTriajeAndActivoTrue(Long idTriaje);

    boolean existsByIdPaciente(Long idPaciente);

    boolean existsByIdPacienteAndActivoTrue(Long idPaciente);

    boolean existsByIdMedico(Long idMedico);

    boolean existsByIdHabitacion(Long idHabitacion);

    boolean existsByIdTriaje(Long idTriaje);

    long countByActivoTrueAndFechaHoraAltaIsNull();
}
