package com.example.hospital.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.hospital.citas.dto.CitasDTO;
import com.example.hospital.consultas.dto.ConsultasDTO;
import com.example.hospital.diagnosticoconsulta.dto.DiagnosticoConsultaDTO;
import com.example.hospital.diagnosticos.dto.DiagnosticosDTO;
import com.example.hospital.asignacionenfermeros.dto.AsignacionEnfermerosDTO;
import com.example.hospital.emergencia.dto.EmergenciaDTO;
import com.example.hospital.habitacion.dto.HabitacionDTO;
import com.example.hospital.hospitalizaciones.dto.HospitalizacionesDTO;
import com.example.hospital.procedimientosemergencia.dto.ProcedimientosEmergenciaDTO;
import com.example.hospital.cuentas.dto.CuentasDTO;
import com.example.hospital.detallefactura.dto.DetalleFacturaDTO;
import com.example.hospital.facturas.dto.FacturasDTO;
import com.example.hospital.medicamentos.dto.MedicamentosDTO;
import com.example.hospital.pagos.dto.PagosDTO;
import com.example.hospital.tratamientomedicamento.dto.TratamientoMedicamentoDTO;
import com.example.hospital.triaje.dto.TriajeDTO;
import com.example.hospital.tratamiento.dto.TratamientoDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Bean Validation de los DTO de citas, consultas, diagnósticos y tratamientos (400 con detalles por campo). */
class ValidacionDtosClinicosTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private <T> Set<String> campos(T dto) {
        return validator.validate(dto).stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    private ConsultasDTO consulta(BigDecimal peso, BigDecimal temp, Integer fc, String presion, Long idCita) {
        return new ConsultasDTO(null, null, null, peso, null, temp, presion, fc, idCita, null);
    }

    @Test
    void cita_exigeFechaHoraEstadoMedicoYPaciente() {
        assertThat(campos(new CitasDTO(null, null, null, null, null, null, null, null, null)))
                .containsExactlyInAnyOrder("fecha", "hora", "idEstado", "idMedico", "idPaciente");
        assertThat(campos(new CitasDTO(null, LocalDate.now(), LocalTime.NOON, "ok", null, 1L, 2L, 3L, null))).isEmpty();
        assertThat(campos(new CitasDTO(null, LocalDate.now(), LocalTime.NOON, "x".repeat(256), null, 1L, 2L, 3L, null)))
                .containsExactly("motivo");
    }

    @Test
    void consulta_validaRangosClinicosYMaximoUnDecimalDeTemperatura() {
        assertThat(campos(consulta(null, null, null, null, null))).containsExactly("idCita");
        assertThat(campos(consulta(new BigDecimal("62.5"), new BigDecimal("37.8"), 78, "120/80", 1L))).isEmpty();
        assertThat(campos(consulta(BigDecimal.ZERO, new BigDecimal("50"), 10, "120/80/1000", 1L)))
                .containsExactlyInAnyOrder("peso", "temperatura", "frecuenciaCardiaca", "presionArterial");
        assertThat(campos(consulta(null, new BigDecimal("37.85"), null, null, 1L))).containsExactly("temperatura");
        assertThat(campos(consulta(new BigDecimal("1000"), null, null, null, 1L))).containsExactly("peso");
    }

    @Test
    void diagnostico_exigeDescripcionTipoYFechaNoFutura() {
        assertThat(campos(new DiagnosticosDTO(null, null, null, " ", null, null)))
                .containsExactlyInAnyOrder("descripcion", "idTipoDiagnostico");
        assertThat(campos(new DiagnosticosDTO(null, null, 1L, "Gripe", LocalDate.now().plusDays(1), null)))
                .containsExactly("fecha");
        assertThat(campos(new DiagnosticosDTO(null, null, 1L, "Gripe", LocalDate.now(), null))).isEmpty();
    }

    @Test
    void diagnosticoConsulta_exigeAmbosIds() {
        assertThat(campos(new DiagnosticoConsultaDTO(null, null))).containsExactlyInAnyOrder("idDiagnostico", "idConsulta");
    }

    @Test
    void tratamiento_exigeDescripcionDiagnosticoYDuracionPositiva() {
        assertThat(campos(new TratamientoDTO(null, "", 0, null, null, null, null, null)))
                .containsExactlyInAnyOrder("descripcion", "duracionDias", "idDiagnostico");
        assertThat(campos(new TratamientoDTO(null, "Reposo", 5, null, null, null, 1L, null))).isEmpty();
    }

    @Test
    void emergencia_exigeMotivoYTriaje_yLimitaElNombreTemporal() {
        assertThat(campos(new EmergenciaDTO(null, null, null, " ", null, null, null, null, null, null, null)))
                .containsExactlyInAnyOrder("motivo", "idTriaje");
        assertThat(campos(new EmergenciaDTO(null, "x".repeat(151), null, "Dolor", null, null, null, null, null, 1L, null)))
                .containsExactly("nombreTemporal");
        assertThat(campos(new EmergenciaDTO(null, "NN", null, "Dolor", null, null, null, null, null, 1L, null))).isEmpty();
    }

    @Test
    void triaje_exigeNivelYLimitaTamanos() {
        assertThat(campos(new TriajeDTO(null, " ", null, null))).containsExactly("nivelDeEmergencia");
        assertThat(campos(new TriajeDTO(null, "x".repeat(51), "y".repeat(256), null)))
                .containsExactlyInAnyOrder("nivelDeEmergencia", "descripcion");
        assertThat(campos(new TriajeDTO(null, "N1", "Reanimación", null))).isEmpty();
    }

    @Test
    void procedimientoEmergencia_exigeCamposYLimitaLaDescripcionA200() {
        assertThat(campos(new ProcedimientosEmergenciaDTO(null, null, "", null, null, null, null)))
                .containsExactlyInAnyOrder("idEmergencia", "descripcion", "idMedico");
        assertThat(campos(new ProcedimientosEmergenciaDTO(null, 1L, "x".repeat(201), null, 1L, null, null)))
                .containsExactly("descripcion");
        assertThat(campos(new ProcedimientosEmergenciaDTO(null, 1L, "x".repeat(200), null, 1L, null, null))).isEmpty();
    }

    @Test
    void habitacion_exigeDatosObligatoriosYLimitaElNumeroA10() {
        assertThat(campos(new HabitacionDTO(null, " ", null, null, null, null, null)))
                .containsExactlyInAnyOrder("numero", "piso", "capacidad", "idTipoHabitacion", "idEstadoHabitacion");
        assertThat(campos(new HabitacionDTO(null, "12345678901", 1, 0, 1L, 1L, null)))
                .containsExactlyInAnyOrder("numero", "capacidad");
        assertThat(campos(new HabitacionDTO(null, "101", 1, 2, 1L, 1L, null))).isEmpty();
    }

    @Test
    void hospitalizacion_exigePacienteMedicoHabitacionYMotivo() {
        assertThat(campos(new HospitalizacionesDTO(null, null, null, null, null, null, null, " ", null, null, null)))
                .containsExactlyInAnyOrder("idPaciente", "idHabitacion", "idMedicoResponsable", "motivoIngreso");
        assertThat(campos(new HospitalizacionesDTO(null, 1L, 1L, 1L, null, null, null, "Neumonia", null, null, null))).isEmpty();
    }

    @Test
    void asignacionEnfermeros_exigeEnfermeroHospitalizacionFechaYTurno() {
        assertThat(campos(new AsignacionEnfermerosDTO(null, null, null, null, null, null)))
                .containsExactlyInAnyOrder("idEnfermero", "idHospitalizacion", "fechaAsignacion", "idTurno");
        assertThat(campos(new AsignacionEnfermerosDTO(null, 1L, 1L, java.time.LocalDate.now(), 1L, null))).isEmpty();
    }

    private MedicamentosDTO medicamento(String comercial, String precio, Integer stock, Integer min) {
        return new MedicamentosDTO(null, comercial, null, null, null, stock, min,
                precio == null ? null : new BigDecimal(precio), null, null);
    }

    @Test
    void medicamento_exigeNombreYPrecio_yValidaRangosYTamanos() {
        assertThat(campos(new MedicamentosDTO(null, null, null, null, null, null, null, null, null, null)))
                .containsExactlyInAnyOrder("nombreComercial", "precio");
        assertThat(campos(medicamento("Paracetamol", "10.50", 0, 0))).isEmpty();
        assertThat(campos(medicamento("   ", "10", null, null))).containsExactly("nombreComercial");
        assertThat(campos(medicamento("x".repeat(151), "10", null, null))).containsExactly("nombreComercial");
        assertThat(campos(medicamento("A", "0", null, null))).containsExactly("precio");
        assertThat(campos(medicamento("A", "-1", null, null))).containsExactly("precio");
        assertThat(campos(medicamento("A", "1.234", null, null))).containsExactly("precio");
        assertThat(campos(medicamento("A", "1", -1, -2))).containsExactlyInAnyOrder("stock", "stockMinimo");
        assertThat(campos(new MedicamentosDTO(null, "A", "g".repeat(151), "p".repeat(101), "l".repeat(101),
                null, null, BigDecimal.ONE, null, null)))
                .containsExactlyInAnyOrder("nombreGenerico", "presentacion", "laboratorio");
    }

    @Test
    void tratamientoMedicamento_exigeAmbosIds() {
        assertThat(campos(new TratamientoMedicamentoDTO(null, null)))
                .containsExactlyInAnyOrder("idMedicamento", "idTratamiento");
        assertThat(campos(new TratamientoMedicamentoDTO(1L, 2L))).isEmpty();
    }

    @Test
    void factura_exigePacienteYMetodoDePago() {
        assertThat(campos(new FacturasDTO(null, null, null, null, null, null, null, null, null, null)))
                .containsExactlyInAnyOrder("idPaciente", "idMetodoPago");
        assertThat(campos(new FacturasDTO(null, 1L, null, null, null, null, null, 1L, null, null))).isEmpty();
    }

    private DetalleFacturaDTO linea(Long idFactura, String concepto, Integer cantidad, String precio) {
        return new DetalleFacturaDTO(null, idFactura, concepto, cantidad, precio == null ? null : new BigDecimal(precio), null);
    }

    @Test
    void detalleFactura_validaObligatoriosTamanosCantidadYPrecio() {
        assertThat(campos(linea(null, null, null, null)))
                .containsExactlyInAnyOrder("idFactura", "concepto", "cantidad", "precioUnitario");
        assertThat(campos(linea(1L, "Consulta", 2, "10.50"))).isEmpty();
        assertThat(campos(linea(1L, "   ", 1, "0"))).containsExactly("concepto");
        assertThat(campos(linea(1L, "x".repeat(201), 1, "1"))).containsExactly("concepto");
        assertThat(campos(linea(1L, "c", 0, "1"))).containsExactly("cantidad");
        assertThat(campos(linea(1L, "c", 100001, "1"))).containsExactly("cantidad");
        assertThat(campos(linea(1L, "c", 1, "-0.01"))).containsExactly("precioUnitario");
        assertThat(campos(linea(1L, "c", 1, "1.234"))).containsExactly("precioUnitario");
        assertThat(campos(linea(1L, "c", 1, "123456789.00"))).containsExactly("precioUnitario");
    }

    private PagosDTO pago(Long idFactura, Long idMetodo, String monto, String referencia) {
        return new PagosDTO(null, idFactura, null, idMetodo, monto == null ? null : new BigDecimal(monto), null, referencia, null);
    }

    @Test
    void pago_validaObligatoriosMontoYReferencia() {
        assertThat(campos(pago(null, null, null, null))).containsExactlyInAnyOrder("idFactura", "idMetodoPago", "monto");
        assertThat(campos(pago(1L, 1L, "10.00", "ref"))).isEmpty();
        assertThat(campos(pago(1L, 1L, "0", null))).containsExactly("monto");
        assertThat(campos(pago(1L, 1L, "-5", null))).containsExactly("monto");
        assertThat(campos(pago(1L, 1L, "1.999", null))).containsExactly("monto");
        assertThat(campos(pago(1L, 1L, "1", "r".repeat(101)))).containsExactly("referencia");
    }

    @Test
    void cuenta_validaPacienteEstadoYSaldo() {
        assertThat(campos(new CuentasDTO(null, null, null, null, null, null, null))).containsExactly("idPaciente");
        assertThat(campos(new CuentasDTO(null, 1L, null, null, "abierta", new BigDecimal("10.50"), null))).isEmpty();
        assertThat(campos(new CuentasDTO(null, 1L, null, null, "Pendiente", null, null))).containsExactly("estado");
        assertThat(campos(new CuentasDTO(null, 1L, null, null, null, new BigDecimal("-1"), null))).containsExactly("saldo");
        assertThat(campos(new CuentasDTO(null, 1L, null, null, null, new BigDecimal("1.234"), null))).containsExactly("saldo");
    }
}
