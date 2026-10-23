package com.example.hospital.reportes;

import com.example.hospital.reportes.dto.HistorialPacienteDTO;
import com.example.hospital.reportes.dto.ResumenDashboardDTO;

public interface ReportesService {

    ResumenDashboardDTO resumen();

    /**
     * Historial del paciente (activo o inactivo; 404 si no existe).
     * @param incluirAnulados incluir registros anulados (activo=false) en todas las secciones
     * @param incluirFinanzas incluir cuentas y facturas (solo para ADMIN/CAJERO; si es false las listas van vacías)
     */
    HistorialPacienteDTO historialPaciente(Long idPaciente, boolean incluirAnulados, boolean incluirFinanzas);
}
