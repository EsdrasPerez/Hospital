package com.example.hospital.reportes;

import com.example.hospital.reportes.dto.HistorialPacienteDTO;
import com.example.hospital.reportes.dto.ResumenDashboardDTO;

public interface ReportesService {

    ResumenDashboardDTO resumen();

    HistorialPacienteDTO historialPaciente(Long idPaciente);
}
