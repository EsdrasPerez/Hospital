package com.example.hospital.reportes.dto;

import com.example.hospital.citas.dto.CitasDTO;
import com.example.hospital.cuentas.dto.CuentasDTO;
import com.example.hospital.emergencia.dto.EmergenciaDTO;
import com.example.hospital.facturas.dto.FacturasDTO;
import com.example.hospital.hospitalizaciones.dto.HospitalizacionesDTO;
import com.example.hospital.paciente.dto.PacienteResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Expediente consolidado de un paciente: datos generales y todo su historial clínico y administrativo")
public record HistorialPacienteDTO(
        @Schema(description = "Datos generales del paciente")
        PacienteResponse paciente,

        @Schema(description = "Todas las citas del paciente")
        List<CitasDTO> citas,

        @Schema(description = "Todas las hospitalizaciones del paciente")
        List<HospitalizacionesDTO> hospitalizaciones,

        @Schema(description = "Todos los ingresos a emergencia del paciente")
        List<EmergenciaDTO> emergencias,

        @Schema(description = "Cuentas hospitalarias del paciente")
        List<CuentasDTO> cuentas,

        @Schema(description = "Facturas emitidas al paciente")
        List<FacturasDTO> facturas
) {}
