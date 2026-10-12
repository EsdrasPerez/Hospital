package com.example.hospital.hospitalizaciones;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalizacionesRepository extends JpaRepository<Hospitalizaciones, Long> {

    // RN-02: Buscar si el paciente ya tiene una hospitalización activa
    boolean existsByIdPacienteAndIdEstadoHosp(Long idPaciente, Long idEstadoHosp);

    // Contar cuántos pacientes hospitalizados activos tiene actualmente una habitación
    long countByIdHabitacionAndIdEstadoHosp(Long idHabitacion, Long idEstadoHosp);

    // Listar por estado
    List<Hospitalizaciones> findByIdEstadoHosp(Long idEstadoHosp);

    // Listar por paciente
    List<Hospitalizaciones> findByIdPaciente(Long idPaciente);

    boolean existsByIdMedicoResponsable(Long idMedico);

    boolean existsByIdMedicoResponsableAndIdEstadoHosp(Long idMedico, Long idEstadoHosp);

    boolean existsByIdEstadoHosp(Long id);
}
