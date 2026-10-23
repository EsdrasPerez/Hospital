package com.example.hospital.habitacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.emergencia.EmergenciaRepository;
import com.example.hospital.estadohabitacion.EstadoHabitacion;
import com.example.hospital.estadohabitacion.EstadoHabitacionRepository;
import com.example.hospital.habitacion.dto.HabitacionDTO;
import com.example.hospital.habitacion.impl.HabitacionServiceImpl;
import com.example.hospital.hospitalizaciones.HospitalizacionesRepository;
import com.example.hospital.shared.catalogos.EstadosCatalogo;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.tiposhabitacion.TiposHabitacion;
import com.example.hospital.tiposhabitacion.TiposHabitacionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HabitacionServiceImplTest {

    private static final Long ACTIVA = 1L;
    private static final Long DISPONIBLE = 1L;
    private static final Long OCUPADA = 2L;
    private static final Long MANTENIMIENTO = 3L;

    @Mock HabitacionRepository habitacionRepository;
    @Mock EstadoHabitacionRepository estadoHabitacionRepository;
    @Mock TiposHabitacionRepository tiposHabitacionRepository;
    @Mock HospitalizacionesRepository hospitalizacionesRepository;
    @Mock EmergenciaRepository emergenciaRepository;
    @Mock EstadosCatalogo estados;
    @InjectMocks HabitacionServiceImpl service;

    @BeforeEach
    void catalogoDeEstados() {
        lenient().when(estados.hospActiva()).thenReturn(ACTIVA);
        lenient().when(estados.habDisponible()).thenReturn(DISPONIBLE);
        lenient().when(estados.habOcupada()).thenReturn(OCUPADA);
        lenient().when(estados.habMantenimiento()).thenReturn(MANTENIMIENTO);
    }

    private Habitacion habitacion(Long id, int piso, int capacidad, Long tipo, Long estado, boolean activo) {
        Habitacion h = new Habitacion();
        h.setIdHabitacion(id);
        h.setNumero("10" + id);
        h.setPiso(piso);
        h.setCapacidad(capacidad);
        h.setIdTipoHabitacion(tipo);
        h.setIdEstadoHabitacion(estado);
        h.setActivo(activo);
        return h;
    }

    private EstadoHabitacion estadoActivo(Long id) {
        EstadoHabitacion e = new EstadoHabitacion();
        e.setIdEstadoHabitacion(id);
        e.setActivo(true);
        return e;
    }

    private void mockCambioDeEstado(Habitacion h, Long nuevoEstado, long ocupacion) {
        when(habitacionRepository.findById(h.getIdHabitacion())).thenReturn(Optional.of(h));
        when(estadoHabitacionRepository.findById(nuevoEstado)).thenReturn(Optional.of(estadoActivo(nuevoEstado)));
        when(hospitalizacionesRepository.countByIdHabitacionAndIdEstadoHosp(h.getIdHabitacion(), ACTIVA)).thenReturn(ocupacion);
    }

    @Test
    void cambiarEstado_aDisponible_conLaHabitacionLlena_lanzaReglaNegocio422() {
        Habitacion h = habitacion(2L, 1, 2, 2L, OCUPADA, true);
        mockCambioDeEstado(h, DISPONIBLE, 2);

        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(2L, DISPONIBLE));

        verify(habitacionRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_aDisponible_conCamaLibre_esPermitido() {
        Habitacion h = habitacion(2L, 1, 2, 2L, OCUPADA, true);
        mockCambioDeEstado(h, DISPONIBLE, 1);
        when(habitacionRepository.save(h)).thenReturn(h);

        assertThat(service.cambiarEstado(2L, DISPONIBLE).idEstadoHabitacion()).isEqualTo(DISPONIBLE);
    }

    @Test
    void cambiarEstado_aOcupada_sinPacientes_lanzaReglaNegocio422() {
        Habitacion h = habitacion(1L, 1, 1, 1L, DISPONIBLE, true);
        mockCambioDeEstado(h, OCUPADA, 0);

        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(1L, OCUPADA));
    }

    @Test
    void cambiarEstado_aMantenimiento_conPacientes_lanzaReglaNegocio422() {
        Habitacion h = habitacion(2L, 1, 2, 2L, OCUPADA, true);
        mockCambioDeEstado(h, MANTENIMIENTO, 1);

        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(2L, MANTENIMIENTO));
    }

    @Test
    void cambiarEstado_aMantenimiento_sinPacientes_esPermitido() {
        Habitacion h = habitacion(1L, 1, 1, 1L, DISPONIBLE, true);
        mockCambioDeEstado(h, MANTENIMIENTO, 0);
        when(habitacionRepository.save(h)).thenReturn(h);

        assertThat(service.cambiarEstado(1L, MANTENIMIENTO).idEstadoHabitacion()).isEqualTo(MANTENIMIENTO);
    }

    @Test
    void cambiarEstado_lanzaNoEncontrado_siLaHabitacionNoExiste() {
        when(habitacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.cambiarEstado(99L, DISPONIBLE));
    }

    private HabitacionDTO dto(String numero, Long estado) {
        return new HabitacionDTO(null, numero, 1, 1, 1L, estado, null);
    }

    private void mockCatalogosValidos(Long estado) {
        TiposHabitacion tipo = new TiposHabitacion();
        tipo.setActivo(true);
        when(tiposHabitacionRepository.findById(1L)).thenReturn(Optional.of(tipo));
        when(estadoHabitacionRepository.findById(estado)).thenReturn(Optional.of(estadoActivo(estado)));
    }

    @Test
    void crear_conNumeroDuplicado_lanzaConflicto409() {
        when(habitacionRepository.existsByNumero("101")).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.crear(dto("101", DISPONIBLE)));

        verify(habitacionRepository, never()).save(any());
    }

    @Test
    void crear_conTipoInexistente_lanzaReglaNegocio422() {
        when(tiposHabitacionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto("101", DISPONIBLE)));
    }

    @Test
    void crear_nacidaOcupada_lanzaReglaNegocio422_porqueNoTienePacientes() {
        mockCatalogosValidos(OCUPADA);

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto("101", OCUPADA)));
    }

    @Test
    void crear_valida_guardaLaHabitacionActiva() {
        mockCatalogosValidos(DISPONIBLE);
        when(habitacionRepository.save(any(Habitacion.class))).thenAnswer(inv -> inv.getArgument(0));

        HabitacionDTO r = service.crear(dto(" 101 ", DISPONIBLE));

        assertThat(r.numero()).isEqualTo("101");
        assertThat(r.activo()).isTrue();
    }

    @Test
    void actualizar_conCambioDeEstadoIncoherente_aplicaLaMismaReglaQuePatch() {
        Habitacion h = habitacion(2L, 1, 1, 1L, OCUPADA, true);
        when(habitacionRepository.findById(2L)).thenReturn(Optional.of(h));
        mockCatalogosValidos(DISPONIBLE);
        when(hospitalizacionesRepository.countByIdHabitacionAndIdEstadoHosp(2L, ACTIVA)).thenReturn(1L);

        assertThrows(ReglaNegocioException.class, () -> service.actualizar(dto("102", DISPONIBLE), 2L));
    }

    @Test
    void buscar_combinaLosFiltros() {
        Habitacion a = habitacion(1L, 1, 1, 1L, DISPONIBLE, true);
        Habitacion b = habitacion(2L, 1, 2, 2L, OCUPADA, true);
        Habitacion c = habitacion(3L, 2, 1, 1L, DISPONIBLE, true);
        Habitacion d = habitacion(4L, 1, 1, 1L, DISPONIBLE, false);
        when(habitacionRepository.findAll()).thenReturn(List.of(a, b, c, d));

        assertThat(service.buscar(1, null, null, false, false)).hasSize(3);
        assertThat(service.buscar(1, 1L, null, false, false)).hasSize(2);
        assertThat(service.buscar(1, 1L, DISPONIBLE, true, false)).extracting(HabitacionDTO::idHabitacion).containsExactly(1L);
        assertThat(service.buscar(null, null, null, false, true)).extracting(HabitacionDTO::idHabitacion).containsExactly(1L, 3L);
        assertThat(service.buscar(null, null, OCUPADA, false, false)).extracting(HabitacionDTO::idHabitacion).containsExactly(2L);
    }
}
