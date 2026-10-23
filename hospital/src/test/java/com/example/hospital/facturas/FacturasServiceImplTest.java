package com.example.hospital.facturas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.estadosfactura.EstadosFactura;
import com.example.hospital.estadosfactura.EstadosFacturaRepository;
import com.example.hospital.facturas.dto.FacturasDTO;
import com.example.hospital.facturas.dto.SaldoFacturaDTO;
import com.example.hospital.facturas.impl.FacturasServiceImpl;
import com.example.hospital.hospitalizaciones.Hospitalizaciones;
import com.example.hospital.hospitalizaciones.HospitalizacionesRepository;
import com.example.hospital.metodospago.MetodosPago;
import com.example.hospital.metodospago.MetodosPagoRepository;
import com.example.hospital.pagos.Pagos;
import com.example.hospital.pagos.PagosRepository;
import com.example.hospital.paciente.Paciente;
import com.example.hospital.paciente.PacienteRepository;
import com.example.hospital.shared.catalogos.EstadosCatalogo;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FacturasServiceImplTest {

    private static final Long PENDIENTE = 1L;
    private static final Long PAGADA = 2L;
    private static final Long CANCELADA = 3L;

    @Mock FacturasRepository facturasRepository;
    @Mock PacienteRepository pacienteRepository;
    @Mock HospitalizacionesRepository hospitalizacionesRepository;
    @Mock MetodosPagoRepository metodosPagoRepository;
    @Mock EstadosFacturaRepository estadosFacturaRepository;
    @Mock PagosRepository pagosRepository;
    @Mock EstadosCatalogo estados;
    @Spy CalculoImportes calculo = new CalculoImportes(new BigDecimal("0.12"));
    @InjectMocks FacturasServiceImpl service;

    @BeforeEach
    void catalogo() {
        lenient().when(estados.facturaPendiente()).thenReturn(PENDIENTE);
        lenient().when(estados.facturaPagada()).thenReturn(PAGADA);
        lenient().when(estados.facturaCancelada()).thenReturn(CANCELADA);
    }

    private FacturasDTO dto(Long idPaciente, Long idHosp, Long idMetodo) {
        // El cliente intenta fijar importes, estado, fecha y activo: el servidor los debe ignorar
        return new FacturasDTO(null, idPaciente, idHosp, LocalDateTime.of(2020, 1, 1, 0, 0), new BigDecimal("999.00"),
                new BigDecimal("99.00"), new BigDecimal("1.00"), idMetodo, PAGADA, false);
    }

    private void stubPacienteMetodoValidos() {
        Paciente p = new Paciente();
        p.setActivo(true);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(p));
        when(metodosPagoRepository.findById(1L)).thenReturn(Optional.of(new MetodosPago(1L, "Efectivo", true)));
    }

    private Facturas factura(Long id, Long idEstado, String subtotal, String iva) {
        Facturas f = new Facturas();
        f.setIdFactura(id);
        f.setIdPaciente(1L);
        f.setSubtotal(new BigDecimal(subtotal));
        f.setIva(new BigDecimal(iva));
        f.setTotal(new BigDecimal(subtotal).add(new BigDecimal(iva)));
        f.setIdMetodoPago(1L);
        f.setIdEstadoFactura(idEstado);
        f.setFechaEmision(LocalDateTime.of(2026, 10, 20, 10, 0));
        f.setActivo(true);
        return f;
    }

    private Pagos pago(String monto, boolean activo) {
        Pagos p = new Pagos();
        p.setMonto(new BigDecimal(monto));
        p.setActivo(activo);
        return p;
    }

    private void estadoExiste(Long id, boolean activo) {
        when(estadosFacturaRepository.findById(id)).thenReturn(Optional.of(new EstadosFactura(id, "x", activo)));
    }

    // ---------- crear ----------

    @Test
    void crear_ignoraImportesEstadoYFechaDelCliente_yNaceVaciaPendienteYActiva() {
        stubPacienteMetodoValidos();
        when(facturasRepository.save(any(Facturas.class))).thenAnswer(inv -> {
            Facturas f = inv.getArgument(0);
            f.setIdFactura(5L);
            return f;
        });

        FacturasDTO r = service.crear(dto(1L, null, 1L));

        assertThat(r.idFactura()).isEqualTo(5L);
        assertThat(r.subtotal()).isEqualByComparingTo("0");
        assertThat(r.iva()).isEqualByComparingTo("0");
        assertThat(r.total()).isEqualByComparingTo("0");
        assertThat(r.idEstadoFactura()).isEqualTo(PENDIENTE);
        assertThat(r.activo()).isTrue();
        assertThat(r.fechaEmision()).isAfter(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    void crear_lanza422_siElPacienteNoExisteOEstaInactivo() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.empty());
        Paciente inactivo = new Paciente();
        inactivo.setActivo(false);
        when(pacienteRepository.findById(2L)).thenReturn(Optional.of(inactivo));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(1L, null, 1L)));
        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(2L, null, 1L)));

        verify(facturasRepository, never()).save(any());
    }

    @Test
    void crear_lanza422_siLaHospitalizacionEsDeOtroPaciente() {
        Paciente p = new Paciente();
        p.setActivo(true);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(p));
        Hospitalizaciones h = new Hospitalizaciones();
        h.setIdPaciente(2L);
        when(hospitalizacionesRepository.findById(9L)).thenReturn(Optional.of(h));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(1L, 9L, 1L)));

        verify(facturasRepository, never()).save(any());
    }

    @Test
    void crear_lanza422_siElMetodoDePagoNoExisteOEstaInactivo() {
        Paciente p = new Paciente();
        p.setActivo(true);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(p));
        when(metodosPagoRepository.findById(99L)).thenReturn(Optional.empty());
        when(metodosPagoRepository.findById(4L)).thenReturn(Optional.of(new MetodosPago(4L, "Seguro", false)));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(1L, null, 99L)));
        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(1L, null, 4L)));

        verify(facturasRepository, never()).save(any());
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_noToca_importesEstadoNiFecha_soloMetodoYHospitalizacion() {
        Facturas f = factura(3L, PENDIENTE, "100.00", "12.00");
        when(facturasRepository.findById(3L)).thenReturn(Optional.of(f));
        when(metodosPagoRepository.findById(2L)).thenReturn(Optional.of(new MetodosPago(2L, "Tarjeta", true)));
        when(facturasRepository.save(f)).thenReturn(f);

        FacturasDTO r = service.actualizar(dto(1L, null, 2L), 3L);

        assertThat(r.subtotal()).isEqualByComparingTo("100.00");
        assertThat(r.iva()).isEqualByComparingTo("12.00");
        assertThat(r.total()).isEqualByComparingTo("112.00");
        assertThat(r.idEstadoFactura()).isEqualTo(PENDIENTE);
        assertThat(r.fechaEmision()).isEqualTo(LocalDateTime.of(2026, 10, 20, 10, 0));
        assertThat(r.activo()).isTrue();
        assertThat(r.idMetodoPago()).isEqualTo(2L);
    }

    @Test
    void actualizar_enFacturaPagada_permiteCambiarSoloElMetodoDePago() {
        Facturas f = factura(3L, PAGADA, "100.00", "12.00");
        when(facturasRepository.findById(3L)).thenReturn(Optional.of(f));
        when(metodosPagoRepository.findById(2L)).thenReturn(Optional.of(new MetodosPago(2L, "Tarjeta", true)));
        when(facturasRepository.save(f)).thenReturn(f);

        assertThat(service.actualizar(dto(1L, null, 2L), 3L).idMetodoPago()).isEqualTo(2L);
    }

    @Test
    void actualizar_lanza422_siSeCambiaLaHospitalizacionDeUnaFacturaPagada() {
        when(facturasRepository.findById(3L)).thenReturn(Optional.of(factura(3L, PAGADA, "100.00", "12.00")));

        assertThrows(ReglaNegocioException.class, () -> service.actualizar(dto(1L, 9L, 1L), 3L));

        verify(facturasRepository, never()).save(any());
    }

    @Test
    void actualizar_lanza422_siSeCambiaElPacienteDeUnaFacturaConPagos() {
        when(facturasRepository.findById(3L)).thenReturn(Optional.of(factura(3L, PENDIENTE, "100.00", "12.00")));
        when(pagosRepository.findByIdFactura(3L)).thenReturn(List.of(pago("10.00", false)));

        assertThrows(ReglaNegocioException.class, () -> service.actualizar(dto(2L, null, 1L), 3L));

        verify(facturasRepository, never()).save(any());
    }

    @Test
    void actualizar_lanza422_siLaFacturaEstaAnulada_y404SiNoExiste() {
        Facturas anulada = factura(3L, PENDIENTE, "0.00", "0.00");
        anulada.setActivo(false);
        when(facturasRepository.findById(3L)).thenReturn(Optional.of(anulada));
        when(facturasRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReglaNegocioException.class, () -> service.actualizar(dto(1L, null, 1L), 3L));
        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizar(dto(1L, null, 1L), 99L));

        verify(facturasRepository, never()).save(any());
    }

    // ---------- cambiarEstado ----------

    @Test
    void cambiarEstado_pendienteAPagada_siTotalMayorACeroYSaldoCero() {
        Facturas f = factura(3L, PENDIENTE, "100.00", "12.00");
        when(facturasRepository.findById(3L)).thenReturn(Optional.of(f));
        estadoExiste(PAGADA, true);
        when(pagosRepository.findByIdFactura(3L)).thenReturn(List.of(pago("112.00", true)));
        when(facturasRepository.save(f)).thenReturn(f);

        assertThat(service.cambiarEstado(3L, PAGADA).idEstadoFactura()).isEqualTo(PAGADA);
    }

    @Test
    void cambiarEstado_aPagada_lanza422_conSaldoPendiente_oTotalCero() {
        Facturas conSaldo = factura(3L, PENDIENTE, "100.00", "12.00");
        Facturas vacia = factura(4L, PENDIENTE, "0.00", "0.00");
        when(facturasRepository.findById(3L)).thenReturn(Optional.of(conSaldo));
        when(facturasRepository.findById(4L)).thenReturn(Optional.of(vacia));
        estadoExiste(PAGADA, true);
        when(pagosRepository.findByIdFactura(3L)).thenReturn(List.of(pago("50.00", true), pago("500.00", false)));
        when(pagosRepository.findByIdFactura(4L)).thenReturn(List.of());

        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(3L, PAGADA));
        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(4L, PAGADA));

        verify(facturasRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_pendienteACancelada_soloSinPagosActivos() {
        Facturas sinPagos = factura(3L, PENDIENTE, "100.00", "12.00");
        Facturas conPagos = factura(4L, PENDIENTE, "100.00", "12.00");
        when(facturasRepository.findById(3L)).thenReturn(Optional.of(sinPagos));
        when(facturasRepository.findById(4L)).thenReturn(Optional.of(conPagos));
        estadoExiste(CANCELADA, true);
        when(pagosRepository.findByIdFactura(3L)).thenReturn(List.of(pago("10.00", false)));
        when(pagosRepository.findByIdFactura(4L)).thenReturn(List.of(pago("10.00", true)));
        when(facturasRepository.save(sinPagos)).thenReturn(sinPagos);

        assertThat(service.cambiarEstado(3L, CANCELADA).idEstadoFactura()).isEqualTo(CANCELADA);
        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(4L, CANCELADA));
    }

    @Test
    void cambiarEstado_pagadaYCanceladaSonTerminales_yMismoEstadoDa422() {
        when(facturasRepository.findById(3L)).thenReturn(Optional.of(factura(3L, PAGADA, "100.00", "12.00")));
        when(facturasRepository.findById(4L)).thenReturn(Optional.of(factura(4L, CANCELADA, "0.00", "0.00")));
        estadoExiste(PENDIENTE, true);
        estadoExiste(PAGADA, true);

        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(3L, PENDIENTE));
        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(4L, PENDIENTE));
        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(3L, PAGADA));

        verify(facturasRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_lanza422_siElEstadoNoExisteOEstaInactivo_y404SiLaFacturaNoExiste() {
        when(facturasRepository.findById(3L)).thenReturn(Optional.of(factura(3L, PENDIENTE, "100.00", "12.00")));
        when(facturasRepository.findById(99L)).thenReturn(Optional.empty());
        when(estadosFacturaRepository.findById(77L)).thenReturn(Optional.empty());
        estadoExiste(PAGADA, false);

        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(3L, 77L));
        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(3L, PAGADA));
        assertThrows(RecursoNoEncontradoException.class, () -> service.cambiarEstado(99L, PAGADA));
    }

    // ---------- eliminar ----------

    @Test
    void eliminar_lanza422_siLaFacturaEstaPagada() {
        when(facturasRepository.findById(3L)).thenReturn(Optional.of(factura(3L, PAGADA, "100.00", "12.00")));

        assertThrows(ReglaNegocioException.class, () -> service.eliminar(3L));

        verify(facturasRepository, never()).save(any());
    }

    @Test
    void eliminar_lanzaConflicto409_siTienePagosActivos() {
        when(facturasRepository.findById(3L)).thenReturn(Optional.of(factura(3L, PENDIENTE, "100.00", "12.00")));
        when(pagosRepository.findByIdFactura(3L)).thenReturn(List.of(pago("10.00", true)));

        assertThrows(DuplicadoException.class, () -> service.eliminar(3L));

        verify(facturasRepository, never()).save(any());
    }

    @Test
    void eliminar_haceBorradoLogico_siEstaPendienteYSoloTienePagosAnulados() {
        Facturas f = factura(3L, PENDIENTE, "100.00", "12.00");
        when(facturasRepository.findById(3L)).thenReturn(Optional.of(f));
        when(pagosRepository.findByIdFactura(3L)).thenReturn(List.of(pago("10.00", false)));

        service.eliminar(3L);

        assertThat(f.getActivo()).isFalse();
        verify(facturasRepository).save(f);
        verify(facturasRepository, never()).deleteById(any());
    }

    @Test
    void eliminar_lanza422_siYaEstaAnulada() {
        Facturas f = factura(3L, PENDIENTE, "0.00", "0.00");
        f.setActivo(false);
        when(facturasRepository.findById(3L)).thenReturn(Optional.of(f));

        assertThrows(ReglaNegocioException.class, () -> service.eliminar(3L));
    }

    // ---------- consultas ----------

    @Test
    void findById_lanzaNoEncontrado_siLaFacturaNoExiste() {
        when(facturasRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.findById(99L));
    }

    @Test
    void buscar_combinaPacienteEstadoFechasYActivas() {
        Facturas a = factura(1L, PENDIENTE, "10.00", "1.20");
        a.setFechaEmision(LocalDateTime.of(2026, 10, 1, 23, 59));
        Facturas b = factura(2L, PAGADA, "10.00", "1.20");
        b.setFechaEmision(LocalDateTime.of(2026, 10, 5, 0, 0));
        Facturas c = factura(3L, PENDIENTE, "10.00", "1.20");
        c.setIdPaciente(2L);
        c.setIdHospitalizacion(7L);
        c.setFechaEmision(LocalDateTime.of(2026, 10, 10, 8, 0));
        Facturas d = factura(4L, PENDIENTE, "10.00", "1.20");
        d.setActivo(false);
        d.setFechaEmision(LocalDateTime.of(2026, 10, 10, 8, 0));
        when(facturasRepository.findAll()).thenReturn(List.of(a, b, c, d));

        assertThat(ids(service.buscar(null, null, null, null, null, false))).containsExactly(1L, 2L, 3L, 4L);
        assertThat(ids(service.buscar(null, null, null, null, null, true))).containsExactly(1L, 2L, 3L);
        assertThat(ids(service.buscar(1L, null, null, null, null, false))).containsExactly(1L, 2L, 4L);
        assertThat(ids(service.buscar(1L, null, PENDIENTE, null, null, false))).containsExactly(1L, 4L);
        assertThat(ids(service.buscar(null, 7L, null, null, null, false))).containsExactly(3L);
        assertThat(ids(service.buscar(null, null, null, LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 10), true)))
                .containsExactly(2L, 3L);
        // límites inclusivos por fecha de emisión (sin pasar por zonas horarias)
        assertThat(ids(service.buscar(null, null, null, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 1), false)))
                .containsExactly(1L);
        assertThat(ids(service.buscar(1L, null, null, LocalDate.of(2026, 10, 1), null, true))).containsExactly(1L, 2L);
    }

    @Test
    void buscar_lanza422_siDesdeEsPosteriorAHasta() {
        assertThrows(ReglaNegocioException.class,
                () -> service.buscar(null, null, null, LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 1), false));
    }

    private List<Long> ids(List<FacturasDTO> lista) {
        return lista.stream().map(FacturasDTO::idFactura).toList();
    }

    @Test
    void obtenerSaldo_restaLosPagosActivosDelTotal_ignorandoLosInactivos() {
        when(facturasRepository.findById(3L)).thenReturn(Optional.of(factura(3L, PENDIENTE, "1000.00", "120.00")));
        when(pagosRepository.findByIdFactura(3L)).thenReturn(List.of(pago("500.00", true), pago("300.00", false)));

        SaldoFacturaDTO s = service.obtenerSaldo(3L);

        assertThat(s.totalPagado()).isEqualByComparingTo("500.00");
        assertThat(s.saldoPendiente()).isEqualByComparingTo("620.00");
        assertThat(s.cantidadPagos()).isEqualTo(1);
        assertThat(s.liquidada()).isFalse();
    }
}
