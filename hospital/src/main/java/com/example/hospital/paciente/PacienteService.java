package com.example.hospital.paciente;

import com.example.hospital.paciente.dto.PacienteRequest;
import com.example.hospital.paciente.dto.PacienteResponse;
import java.util.List;

public interface PacienteService {
    List<PacienteResponse> listar();
    List<PacienteResponse> listarTodos();
    PacienteResponse obtener(Long id);
    PacienteResponse buscarPorDpi(String dpi);
    List<PacienteResponse> buscar(String criterio);
    PacienteResponse crear(PacienteRequest request);
    PacienteResponse actualizar(Long id, PacienteRequest request);
    void eliminar(Long id);
}
