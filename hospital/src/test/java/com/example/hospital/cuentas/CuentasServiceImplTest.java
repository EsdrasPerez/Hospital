package com.example.hospital.cuentas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.cuentas.dto.CuentasDTO;
import com.example.hospital.cuentas.impl.CuentasServiceImpl;
import com.example.hospital.hospitalizaciones.Hospitalizaciones;
import com.example.hospital.hospitalizaciones.HospitalizacionesRepository;
import com.example.hospital.paciente.Paciente;
import com.example.hospital.paciente.PacienteRepository;
import com.example.hospital.pagos.PagosRepository;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CuentasServiceImplTest {

    @Mock CuentasRepository cuentasRepository;
    @Mock PacienteRepository pacienteRepository;
    @Mock HospitalizacionesRepository hospitalizacionesRepository;
    @Mock PagosRepository pagosRepository;
    @InjectMocks CuentasServiceImpl service;

    @BeforeEach
    void guardar() {
        lenient().when(cuentasRepository.save(any(Cuentas.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Cuentas cuenta(Long id, String estado, String saldo) {
        Cuentas c = new Cuentas();
        c.setIdCuenta(id);
        c.setIdPaciente(1L);
        c.setEstado(estado);
        c.setSaldo(new BigDecimal(saldo));
        c.setActivo(true);
        return c;
    }

    private CuentasDTO dto(Long idHosp, String estado, String saldo) {
        return new CuentasDTO(null, 1L, idHosp, null, estado, saldo == null ? null : new BigDecimal(saldo), null);
    }

    private void pacienteActivo() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(new Paciente()));
    }

    // ---------- crear ----------

    @Test
    void crear_abreLaCuentaAbiertaActivaConFechaDelServidor_ignorandoEstadoYActivoDelCliente() {
        pacienteActivo();
        CuentasDTO conEstado = new CuentasDTO(null, 1L, null, null, "Cerrada", new BigDecimal("150.00"), false);

        CuentasDTO r = service.crear(conEstado);

        assertThat(r.estado()).isEqualTo(CuentasServiceImpl.ESTADO_ABIERTA);
        assertThat(r.saldo()).isEqualByComparingTo("150.00");
        assertThat(r.activo()).isTrue();
        assertThat(r.fechaApertura()).isNotNull();
    }

    @Test
    void crear_saldoPorDefectoCero() {
        pacienteActivo();

        assertThat(service.crear(dto(null, null, null)).saldo()).isEqualByComparingTo("0");
    }

    @Test
    void crear_lanza422_siElPacienteNoExisteOEstaInactivo() {
        Paciente inactivo = new Paciente();
        inactivo.setActivo(false);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.empty());
        when(pacienteRepository.findById(2L)).thenReturn(Optional.of(inactivo));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(null, null, null)));
        assertThrows(ReglaNegocioException.class, () -> service.crear(new CuentasDTO(null, 2L, null, null, null, null, null)));

        verify(cuentasRepository, never()).save(any());
    }

    @Test
    void crear_lanza422_siLaHospitalizacionNoExisteOEsDeOtroPaciente() {
        pacienteActivo();
        Hospitalizaciones ajena = new Hospitalizaciones();
        ajena.setIdPaciente(2L);
        when(hospitalizacionesRepository.findById(9L)).thenReturn(Optional.empty());
        when(hospitalizacionesRepository.findById(8L)).thenReturn(Optional.of(ajena));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(9L, null, null)));
        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(8L, null, null)));

        verify(cuentasRepository, never()).save(any());
    }

    @Test
    void crear_lanzaConflicto409_siLaHospitalizacionYaTieneUnaCuentaActiva() {
        pacienteActivo();
        Hospitalizaciones h = new Hospitalizaciones();
        h.setIdPaciente(1L);
        when(hospitalizacionesRepository.findById(3L)).thenReturn(Optional.of(h));
        when(cuentasRepository.existsByIdHospitalizacionAndActivoTrue(3L)).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.crear(dto(3L, null, null)));

        verify(cuentasRepository, never()).save(any());
    }

    @Test
    void crear_lanza422_siElSaldoEsNegativo() {
        pacienteActivo();

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(null, null, "-1.00")));
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_ignoraElSaldoYActivo_cambiaEstadoYHospitalizacion() {
        Cuentas c = cuenta(1L, "Abierta", "500.00");
        when(cuentasRepository.findById(1L)).thenReturn(Optional.of(c));
        pacienteActivo();

        CuentasDTO r = service.actualizar(new CuentasDTO(1L, 1L, null, null, "cerrada", new BigDecimal("1.00"), false), 1L);

        assertThat(r.saldo()).isEqualByComparingTo("500.00");
        assertThat(r.estado()).isEqualTo("Cerrada");
        assertThat(r.activo()).isTrue();
    }

    @Test
    void actualizar_liquidadaSoloConSaldoCero_yPuedeReabrirse() {
        Cuentas conSaldo = cuenta(1L, "Abierta", "500.00");
        Cuentas liquidada = cuenta(2L, "Liquidada", "0.00");
        when(cuentasRepository.findById(1L)).thenReturn(Optional.of(conSaldo));
        when(cuentasRepository.findById(2L)).thenReturn(Optional.of(liquidada));
        pacienteActivo();

        assertThrows(ReglaNegocioException.class, () -> service.actualizar(dto(null, "Liquidada", null), 1L));
        assertThat(service.actualizar(dto(null, "Abierta", null), 2L).estado()).isEqualTo("Abierta");
    }

    @Test
    void actualizar_lanza422_conEstadoInvalido_oCuentaDesactivada_oPacienteConPagos_y404SiNoExiste() {
        Cuentas c = cuenta(1L, "Abierta", "5.00");
        Cuentas desactivada = cuenta(2L, "Abierta", "5.00");
        desactivada.setActivo(false);
        when(cuentasRepository.findById(1L)).thenReturn(Optional.of(c));
        when(cuentasRepository.findById(2L)).thenReturn(Optional.of(desactivada));
        when(cuentasRepository.findById(99L)).thenReturn(Optional.empty());
        pacienteActivo();
        Paciente otro = new Paciente();
        when(pacienteRepository.findById(2L)).thenReturn(Optional.of(otro));
        when(pagosRepository.existsByIdCuenta(1L)).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> service.actualizar(dto(null, "Pendiente", null), 1L));
        assertThrows(ReglaNegocioException.class, () -> service.actualizar(dto(null, "Abierta", null), 2L));
        assertThrows(ReglaNegocioException.class,
                () -> service.actualizar(new CuentasDTO(1L, 2L, null, null, null, null, null), 1L));
        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizar(dto(null, null, null), 99L));
    }

    // ---------- ajustar saldo ----------

    @Test
    void findById_lanzaNoEncontrado_siLaCuentaNoExiste() {
        when(cuentasRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.findById(99L));
    }

    @Test
    void ajustarSaldo_lanzaNoEncontrado_siLaCuentaNoExiste_yNoGuarda() {
        when(cuentasRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.ajustarSaldo(99L, BigDecimal.TEN));

        verify(cuentasRepository, never()).save(any());
    }

    @Test
    void ajustarSaldo_pasaALiquidada_cuandoElSaldoLlegaACero_yReabreSiVuelveACargarse() {
        Cuentas c = cuenta(1L, CuentasServiceImpl.ESTADO_ABIERTA, "500.00");
        when(cuentasRepository.findById(1L)).thenReturn(Optional.of(c));

        CuentasDTO liquidada = service.ajustarSaldo(1L, new BigDecimal("-500.00"));
        assertThat(liquidada.saldo()).isEqualByComparingTo("0");
        assertThat(liquidada.estado()).isEqualTo(CuentasServiceImpl.ESTADO_LIQUIDADA);

        CuentasDTO reabierta = service.ajustarSaldo(1L, new BigDecimal("75.25"));
        assertThat(reabierta.saldo()).isEqualByComparingTo("75.25");
        assertThat(reabierta.estado()).isEqualTo(CuentasServiceImpl.ESTADO_ABIERTA);
    }

    @Test
    void ajustarSaldo_lanza422_conMontoCeroNuloOMasDeDosDecimales() {
        when(cuentasRepository.findById(1L)).thenReturn(Optional.of(cuenta(1L, "Abierta", "10.00")));

        assertThrows(ReglaNegocioException.class, () -> service.ajustarSaldo(1L, null));
        assertThrows(ReglaNegocioException.class, () -> service.ajustarSaldo(1L, BigDecimal.ZERO));
        assertThrows(ReglaNegocioException.class, () -> service.ajustarSaldo(1L, new BigDecimal("1.005")));

        verify(cuentasRepository, never()).save(any());
    }

    @Test
    void ajustarSaldo_lanza422_siElSaldoQuedariaNegativoOSuperaElLimite() {
        Cuentas c = cuenta(1L, "Abierta", "10.00");
        when(cuentasRepository.findById(1L)).thenReturn(Optional.of(c));

        assertThrows(ReglaNegocioException.class, () -> service.ajustarSaldo(1L, new BigDecimal("-10.01")));
        assertThrows(ReglaNegocioException.class, () -> service.ajustarSaldo(1L, new BigDecimal("99999999.99")));

        assertThat(c.getSaldo()).isEqualByComparingTo("10.00");
        verify(cuentasRepository, never()).save(any());
    }

    @Test
    void ajustarSaldo_lanza422_enCuentaCerradaODesactivada() {
        Cuentas cerrada = cuenta(1L, "Cerrada", "10.00");
        Cuentas desactivada = cuenta(2L, "Abierta", "10.00");
        desactivada.setActivo(false);
        when(cuentasRepository.findById(1L)).thenReturn(Optional.of(cerrada));
        when(cuentasRepository.findById(2L)).thenReturn(Optional.of(desactivada));

        assertThrows(ReglaNegocioException.class, () -> service.ajustarSaldo(1L, BigDecimal.ONE));
        assertThrows(ReglaNegocioException.class, () -> service.ajustarSaldo(2L, BigDecimal.ONE));

        verify(cuentasRepository, never()).save(any());
    }

    // ---------- eliminar ----------

    @Test
    void eliminar_lanzaNoEncontrado_siLaCuentaNoExiste_yNoGuarda() {
        when(cuentasRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(99L));

        verify(cuentasRepository, never()).save(any());
    }

    @Test
    void eliminar_haceBorradoLogico_siLaCuentaNoTienePagos() {
        Cuentas c = cuenta(1L, CuentasServiceImpl.ESTADO_ABIERTA, "0");
        when(cuentasRepository.findById(1L)).thenReturn(Optional.of(c));
        when(pagosRepository.existsByIdCuenta(1L)).thenReturn(false);

        service.eliminar(1L);

        assertThat(c.getActivo()).isFalse();
        verify(cuentasRepository).save(c);
        verify(cuentasRepository, never()).deleteById(any());
    }

    @Test
    void eliminar_lanzaConflicto409_siTienePagos_y422SiYaEstaDesactivada() {
        Cuentas c = cuenta(1L, "Abierta", "0");
        Cuentas inactiva = cuenta(2L, "Abierta", "0");
        inactiva.setActivo(false);
        when(cuentasRepository.findById(1L)).thenReturn(Optional.of(c));
        when(cuentasRepository.findById(2L)).thenReturn(Optional.of(inactiva));
        when(pagosRepository.existsByIdCuenta(1L)).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.eliminar(1L));
        assertThrows(ReglaNegocioException.class, () -> service.eliminar(2L));

        assertThat(c.getActivo()).isTrue();
        verify(cuentasRepository, never()).save(any());
    }

    // ---------- filtros ----------

    @Test
    void buscar_combinaPacienteHospitalizacionEstadoYActivas() {
        Cuentas a = cuenta(1L, "Abierta", "10.00");
        a.setIdHospitalizacion(5L);
        Cuentas b = cuenta(2L, "Liquidada", "0.00");
        Cuentas c = cuenta(3L, "Abierta", "1.00");
        c.setIdPaciente(2L);
        Cuentas d = cuenta(4L, "Abierta", "1.00");
        d.setActivo(false);
        when(cuentasRepository.findAll()).thenReturn(List.of(a, b, c, d));

        assertThat(ids(service.buscar(null, null, null, false))).containsExactly(1L, 2L, 3L, 4L);
        assertThat(ids(service.buscar(null, null, null, true))).containsExactly(1L, 2L, 3L);
        assertThat(ids(service.buscar(1L, null, null, false))).containsExactly(1L, 2L, 4L);
        assertThat(ids(service.buscar(1L, null, "abierta", true))).containsExactly(1L);
        assertThat(ids(service.buscar(null, 5L, null, false))).containsExactly(1L);
        assertThat(ids(service.buscar(1L, null, "Liquidada", false))).containsExactly(2L);
        assertThrows(ReglaNegocioException.class, () -> service.buscar(null, null, "Otro", false));
    }

    private List<Long> ids(List<CuentasDTO> lista) {
        return lista.stream().map(CuentasDTO::idCuenta).toList();
    }
}
