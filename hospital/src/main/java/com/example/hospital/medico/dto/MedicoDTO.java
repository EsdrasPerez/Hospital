package com.example.hospital.medico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MedicoDTO(
    Long idMedico,
    @NotBlank(message = "El número de colegiado es obligatorio")
    @Size(max = 30, message = "El número de colegiado no puede superar los 30 caracteres")
    String noColegiado,
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    String nombre,
    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 100, message = "El apellido paterno no puede superar los 100 caracteres")
    String apellidoPaterno,
    @Size(max = 100, message = "El apellido materno no puede superar los 100 caracteres")
    String apellidoMaterno,
    @Size(min = 7, max = 20, message = "El teléfono debe contener entre 7 y 20 caracteres")
    String telefono,
    @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "El formato del correo electrónico no es válido")
    String email,
    Boolean estado
) {}
