package com.example.hospital.paciente;

import com.example.hospital.paciente.dto.PacienteRequest;
import com.example.hospital.paciente.dto.PacienteResumenDTO;
import com.example.hospital.paciente.dto.PacienteResponse;
import java.util.List;

public interface PacienteService {
    List<PacienteResponse> listar();
    List<PacienteResponse> listarTodos();
    /** Solo pacientes activos (404 si no existe o está inactivo). Lo usan otros servicios para validar. */
    PacienteResponse obtener(Long id);
    /** Detalle de un paciente activo o inactivo (404 solo si no existe). */
    PacienteResponse obtenerDetalle(Long id);
    PacienteResponse buscarPorDpi(String dpi);
    List<PacienteResponse> buscar(String criterio);
    /** Busca por nombre, apellidos o DPI; si `todos` es true incluye también los inactivos. */
    List<PacienteResponse> buscar(String criterio, boolean todos);
    /** Búsqueda mínima para caja: solo pacientes ACTIVOS, `q` de al menos 2 caracteres, máximo 20 resultados. */
    List<PacienteResumenDTO> buscarResumen(String q);
    /** Resumen mínimo de un paciente activo o inactivo (404 solo si no existe). */
    PacienteResumenDTO resumen(Long id);
    PacienteResponse crear(PacienteRequest request);
    PacienteResponse actualizar(Long id, PacienteRequest request);
    void eliminar(Long id);
}
