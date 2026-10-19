package com.example.hospital.paciente;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    List<Paciente> findByActivoTrue();

    Optional<Paciente> findByIdPacienteAndActivoTrue(Long idPaciente);

    boolean existsByDpiPaciente(String dpiPaciente);

    boolean existsByDpiPacienteAndIdPacienteNot(String dpiPaciente, Long idPaciente);

    Optional<Paciente> findByDpiPaciente(String dpiPaciente);

    Optional<Paciente> findByDpiPacienteAndActivoTrue(String dpiPaciente);

    boolean existsByCorreoElectronico(String correoElectronico);

    boolean existsByCorreoElectronicoAndIdPacienteNot(String correoElectronico, Long idPaciente);

    List<Paciente> findByNombreContainingIgnoreCaseOrApellidoPaternoContainingIgnoreCaseOrApellidoMaternoContainingIgnoreCaseOrDpiPacienteContainingIgnoreCase(
            String nombre, String apellidoPaterno, String apellidoMaterno, String dpiPaciente);

    // RN-TS-02: Verificar si algún paciente tiene asignado este tipo de sangre
    boolean existsByIdTipoDeSangre(Long idTipoDeSangre);

    boolean existsByIdAlergias(Long idAlergias);

    long countByActivoTrue();
}
