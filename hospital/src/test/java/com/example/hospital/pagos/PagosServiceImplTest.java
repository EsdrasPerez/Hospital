package com.example.hospital.pagos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.cuentas.Cuentas;
import com.example.hospital.cuentas.CuentasRepository;
import com.example.hospital.facturas.CalculoImportes;
import com.example.hospital.facturas.Facturas;
import com.example.hospital.facturas.FacturasRepository;
import com.example.hospital.metodospago.MetodosPago;
import com.example.hospital.metodospago.MetodosPagoRepository;
import com.example.hospital.pagos.dto.PagosDTO;
import com.example.hospital.pagos.impl.PagosServiceImpl;
import com.example.hospital.shared.catalogos.EstadosCatalogo;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import java.math.BigDecimal;
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
class PagosServiceImplTest {

    private static final Long PENDIENTE = 1L;
    private static final Long PAGADA = 2L;
    private static final Long CANCELADA = 3L;

    @Mock PagosRepository pagosRepository;
    @Mock FacturasRepository facturasRepository;
    @Mock MetodosPagoRepository metodosPagoRepository;
    @Mock CuentasRepository cuentasRepository;
    @Mock EstadosCatalogo estados;
    @Spy CalculoImportes calculo = new CalculoImportes(new BigDecimal("0.12"));
    @InjectMocks PagosServiceImpl service;

    @BeforeEach
    void catalogo() {
        lenient().when(estados.facturaPendiente()).thenReturn(PENDIENTE);
        lenient().when(estados.facturaPagada()).thenReturn(PAGADA);
        lenient().when(estados.facturaCancelada()).thenReturn(CANCELADA);
        lenient().when(pagosRepository.save(any(Pagos.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(facturasRepository.save(any(Facturas.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Facturas factura(Long id, Long estado, String total) {
        Facturas f = new Facturas();
        f.setIdFactura(id);
        f.setIdPaciente(1L);
        f.setTotal(new BigDecimal(total));
        f.setIdEstadoFactura(estado);
        f.setActivo(true);
        return f;
    }

    private Pagos pago(Long id, Long idFactura, String monto, boolean activo) {
        Pagos p = new Pagos();
        p.setIdPago(id);
        p.setIdFactura(idFactura);
        p.setIdMetodoPago(1L);
        p.setIdCuenta(null);
        p.setMonto(new BigDecimal(monto));
        p.setActivo(activo);
        return p;
    }

    private PagosDTO dto(Long idFactura, String monto, Long idCuenta) {
        return new PagosDTO(null, idFactura, idCuenta, 1L, new BigDecimal(monto), null, "ref", null);
    }

    private void metodoActivo() {
        when(metodosPagoRepository.findById(1L)).thenReturn(Optional.of(new MetodosPago(1L, "Efectivo", true)));
    }

    // ---------- crear ----------

    @Test
    void crear_registraPagoParcial_yLaFacturaSigueEnPendiente() {
        Facturas f = factura(1L, PENDIENTE, "210.00");
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(f));
        metodoActivo();
        when(pagosRepository.findByIdFactura(1L)).thenReturn(List.of(pago(9L, 1L, "50.00", true)));

        PagosDTO r = service.crear(dto(1L, "100.00", null));

        assertThat(r.activo()).isTrue();
        assertThat(r.fechaPago()).isNotNull();
        assertThat(f.getIdEstadoFactura()).isEqualTo(PENDIENTE);
        verify(facturasRepository, never()).save(any());
    }

    @Test
    void crear_siElSaldoQuedaEnCero_laFacturaPasaAutomaticamenteAPagada() {
        Facturas f = factura(1L, PENDIENTE, "210.00");
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(f));
        metodoActivo();
        when(pagosRepository.findByIdFactura(1L)).thenReturn(List.of(pago(9L, 1L, "100.00", true), pago(8L, 1L, "999.00", false)));

        service.crear(dto(1L, "110.00", null));

        assertThat(f.getIdEstadoFactura()).isEqualTo(PAGADA);
        verify(facturasRepository).save(f);
    }

    @Test
    void crear_lanza422_siElMontoSuperaElSaldoPendiente_yNoGuarda() {
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(factura(1L, PENDIENTE, "210.00")));
        metodoActivo();
        when(pagosRepository.findByIdFactura(1L)).thenReturn(List.of(pago(9L, 1L, "100.00", true)));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(1L, "110.01", null)));

        verify(pagosRepository, never()).save(any());
    }

    @Test
    void crear_lanza422_siElMontoEsCeroONegativo() {
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(factura(1L, PENDIENTE, "210.00")));
        metodoActivo();

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(1L, "0", null)));
        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(1L, "-5", null)));

        verify(pagosRepository, never()).save(any());
    }

    @Test
    void crear_lanza422_siLaFacturaNoEstaPendiente_oEstaAnulada() {
        Facturas anulada = factura(4L, PENDIENTE, "10.00");
        anulada.setActivo(false);
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(factura(1L, PAGADA, "210.00")));
        when(facturasRepository.findById(2L)).thenReturn(Optional.of(factura(2L, CANCELADA, "0.00")));
        when(facturasRepository.findById(4L)).thenReturn(Optional.of(anulada));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(1L, "1", null)));
        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(2L, "1", null)));
        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(4L, "1", null)));

        verify(pagosRepository, never()).save(any());
    }

    @Test
    void crear_lanza422_siLaFacturaElMetodoOLaCuentaNoExistenOEstanInactivos() {
        when(facturasRepository.findById(99L)).thenReturn(Optional.empty());
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(factura(1L, PENDIENTE, "210.00")));
        when(metodosPagoRepository.findById(1L)).thenReturn(Optional.of(new MetodosPago(1L, "Efectivo", true)));
        when(metodosPagoRepository.findById(5L)).thenReturn(Optional.empty());
        when(metodosPagoRepository.findById(4L)).thenReturn(Optional.of(new MetodosPago(4L, "Seguro", false)));
        when(cuentasRepository.findById(77L)).thenReturn(Optional.empty());
        Cuentas inactiva = new Cuentas();
        inactiva.setIdPaciente(1L);
        inactiva.setActivo(false);
        when(cuentasRepository.findById(78L)).thenReturn(Optional.of(inactiva));

        // factura inexistente -> 422 (antes 404)
        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(99L, "1", null)));
        // método inexistente / inactivo -> 422 (antes 404)
        assertThrows(ReglaNegocioException.class,
                () -> service.crear(new PagosDTO(null, 1L, null, 5L, BigDecimal.ONE, null, null, null)));
        assertThrows(ReglaNegocioException.class,
                () -> service.crear(new PagosDTO(null, 1L, null, 4L, BigDecimal.ONE, null, null, null)));
        // cuenta inexistente -> 422 (antes 500) / inactiva -> 422
        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(1L, "1", 77L)));
        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(1L, "1", 78L)));

        verify(pagosRepository, never()).save(any());
    }

    @Test
    void crear_lanza422_siLaCuentaEsDeOtroPaciente_yAceptaLaDelMismoPaciente() {
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(factura(1L, PENDIENTE, "210.00")));
        metodoActivo();
        Cuentas ajena = new Cuentas();
        ajena.setIdPaciente(2L);
        ajena.setActivo(true);
        Cuentas propia = new Cuentas();
        propia.setIdPaciente(1L);
        propia.setActivo(true);
        when(cuentasRepository.findById(10L)).thenReturn(Optional.of(ajena));
        when(cuentasRepository.findById(11L)).thenReturn(Optional.of(propia));
        when(pagosRepository.findByIdFactura(1L)).thenReturn(List.of());

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(1L, "10", 10L)));
        assertThat(service.crear(dto(1L, "10", 11L)).idCuenta()).isEqualTo(11L);
    }

    @Test
    void crear_lanza422_siLaFechaDePagoEsFutura() {
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(factura(1L, PENDIENTE, "210.00")));
        metodoActivo();

        PagosDTO futuro = new PagosDTO(null, 1L, null, 1L, BigDecimal.TEN, LocalDateTime.now().plusDays(1), null, null);
        assertThrows(ReglaNegocioException.class, () -> service.crear(futuro));
    }

    // ---------- anular ----------

    @Test
    void eliminar_anulaElPago_yUnaFacturaPagadaVuelveAPendiente() {
        Pagos p = pago(5L, 1L, "210.00", true);
        Facturas f = factura(1L, PAGADA, "210.00");
        when(pagosRepository.findById(5L)).thenReturn(Optional.of(p));
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(f));

        service.eliminar(5L);

        assertThat(p.getActivo()).isFalse();
        assertThat(f.getIdEstadoFactura()).isEqualTo(PENDIENTE);
        verify(pagosRepository).save(p);
        verify(pagosRepository, never()).deleteById(any());
    }

    @Test
    void eliminar_conFacturaPendiente_anulaSinCambiarElEstado() {
        Pagos p = pago(5L, 1L, "10.00", true);
        Facturas f = factura(1L, PENDIENTE, "210.00");
        when(pagosRepository.findById(5L)).thenReturn(Optional.of(p));
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(f));

        service.eliminar(5L);

        assertThat(p.getActivo()).isFalse();
        verify(facturasRepository, never()).save(any());
    }

    @Test
    void eliminar_lanza422_siLaFacturaEstaCancelada_oElPagoYaEstaAnulado() {
        Pagos activo = pago(5L, 1L, "10.00", true);
        Pagos anulado = pago(6L, 1L, "10.00", false);
        when(pagosRepository.findById(5L)).thenReturn(Optional.of(activo));
        when(pagosRepository.findById(6L)).thenReturn(Optional.of(anulado));
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(factura(1L, CANCELADA, "210.00")));

        assertThrows(ReglaNegocioException.class, () -> service.eliminar(5L));
        assertThrows(ReglaNegocioException.class, () -> service.eliminar(6L));

        assertThat(activo.getActivo()).isTrue();
        verify(pagosRepository, never()).save(any());
    }

    @Test
    void eliminar_lanza404_siElPagoNoExiste() {
        when(pagosRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(99L));
    }

    // ---------- actualizar (corregir método y referencia) ----------

    @Test
    void actualizar_corrigeSoloMetodoYReferencia() {
        Pagos p = pago(5L, 1L, "100.00", true);
        when(pagosRepository.findById(5L)).thenReturn(Optional.of(p));
        when(metodosPagoRepository.findById(2L)).thenReturn(Optional.of(new MetodosPago(2L, "Tarjeta", true)));

        PagosDTO r = service.actualizar(new PagosDTO(5L, 1L, null, 2L, new BigDecimal("100.00"), null, " voucher 7 ", null), 5L);

        assertThat(r.idMetodoPago()).isEqualTo(2L);
        assertThat(r.referencia()).isEqualTo("voucher 7");
        assertThat(r.monto()).isEqualByComparingTo("100.00");
    }

    @Test
    void actualizar_lanza422_siSeIntentaCambiarFacturaMontoCuentaOAnular() {
        Pagos p = pago(5L, 1L, "100.00", true);
        p.setIdCuenta(3L);
        when(pagosRepository.findById(5L)).thenReturn(Optional.of(p));

        assertThrows(ReglaNegocioException.class,
                () -> service.actualizar(new PagosDTO(5L, 2L, null, 1L, new BigDecimal("100.00"), null, null, null), 5L));
        assertThrows(ReglaNegocioException.class,
                () -> service.actualizar(new PagosDTO(5L, 1L, null, 1L, new BigDecimal("90.00"), null, null, null), 5L));
        assertThrows(ReglaNegocioException.class,
                () -> service.actualizar(new PagosDTO(5L, 1L, 4L, 1L, new BigDecimal("100.00"), null, null, null), 5L));
        assertThrows(ReglaNegocioException.class,
                () -> service.actualizar(new PagosDTO(5L, 1L, null, 1L, new BigDecimal("100.00"), null, null, false), 5L));

        verify(pagosRepository, never()).save(any());
    }

    @Test
    void actualizar_lanza422_siElPagoEstaAnulado_noLoReactiva() {
        Pagos anulado = pago(5L, 1L, "100.00", false);
        when(pagosRepository.findById(5L)).thenReturn(Optional.of(anulado));

        assertThrows(ReglaNegocioException.class,
                () -> service.actualizar(new PagosDTO(5L, 1L, null, 1L, new BigDecimal("100.00"), null, null, true), 5L));

        assertThat(anulado.getActivo()).isFalse();
        verify(pagosRepository, never()).save(any());
    }

    // ---------- consultas ----------

    @Test
    void findByFactura_soloActivosEsCombinable_y404SiNoExiste() {
        when(facturasRepository.existsById(1L)).thenReturn(true);
        when(facturasRepository.existsById(99L)).thenReturn(false);
        when(pagosRepository.findByIdFactura(1L)).thenReturn(List.of(pago(1L, 1L, "10", true), pago(2L, 1L, "20", false)));

        assertThat(service.findByFactura(1L, false)).hasSize(2);
        assertThat(service.findByFactura(1L, true)).extracting(PagosDTO::idPago).containsExactly(1L);
        assertThrows(RecursoNoEncontradoException.class, () -> service.findByFactura(99L, false));
    }

    @Test
    void findByCuenta_validaQueLaCuentaExista404_yFiltraActivos() {
        when(cuentasRepository.existsById(2L)).thenReturn(true);
        when(cuentasRepository.existsById(99L)).thenReturn(false);
        when(pagosRepository.findByIdCuenta(2L)).thenReturn(List.of(pago(1L, 1L, "10", true), pago(2L, 1L, "20", false)));

        assertThat(service.findByCuenta(2L, false)).hasSize(2);
        assertThat(service.findByCuenta(2L, true)).hasSize(1);
        assertThrows(RecursoNoEncontradoException.class, () -> service.findByCuenta(99L, false));
    }
}
