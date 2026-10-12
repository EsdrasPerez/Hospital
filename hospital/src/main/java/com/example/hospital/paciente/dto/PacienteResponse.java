package com.example.hospital.paciente.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacienteResponse(
        Long idPaciente,
        String dpiPaciente,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        LocalDate fechaNacimiento,
        String sexo,
        String telefono,
        String correoElectronico,
        String direccion,
        LocalDateTime fechaRegistro,
        Long idAlergias,
        Long idTipoDeSangre,
        Boolean activo
) {}
