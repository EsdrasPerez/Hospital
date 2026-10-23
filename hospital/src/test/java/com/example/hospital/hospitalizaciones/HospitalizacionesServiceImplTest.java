package com.example.hospital.hospitalizaciones;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.clinicas.Clinicas;
import com.example.hospital.clinicas.ClinicasRepository;
import com.example.hospital.habitacion.Habitacion;
import com.example.hospital.habitacion.HabitacionRepository;
import com.example.hospital.estadoshospitalizacion.EstadosHospitalizacion;
import com.example.hospital.estadoshospitalizacion.EstadosHospitalizacionRepository;
import com.example.hospital.hospitalizaciones.dto.HospitalizacionResumenDTO;
import com.example.hospital.hospitalizaciones.dto.HospitalizacionesDTO;
import com.example.hospital.hospitalizaciones.impl.HospitalizacionesServiceImpl;
import com.example.hospital.medico.Medico;
import com.example.hospital.medico.MedicoRepository;
import com.example.hospital.paciente.Paciente;
import com.example.hospital.paciente.PacienteRepository;
import com.example.hospital.shared.catalogos.EstadosCatalogo;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HospitalizacionesServiceImplTest {

    private static final Long ACTIVA = 1L;
    private static final Long ALTA = 2L;
    private static final Long DISPONIBLE = 1L;
    private static final Long OCUPADA = 2L;

    @Mock HospitalizacionesRepository hospitalizacionesRepository;
    @Mock PacienteRepository pacienteRepository;
    @Mock HabitacionRepository habitacionRepository;
    @Mock MedicoRepository medicoRepository;
    @Mock ClinicasRepository clinicasRepository;
    @Mock EstadosCatalogo estados;
    @Mock EstadosHospitalizacionRepository estadosHospitalizacionRepository;
    @InjectMocks HospitalizacionesServiceImpl service;

    private static final Long MANTENIMIENTO = 3L;

    @BeforeEach
    void catalogoDeEstados() {
        // Los estados se resuelven por nombre mediante EstadosCatalogo (aquí con los ids habituales)
        lenient().when(estados.hospActiva()).thenReturn(ACTIVA);
        lenient().when(estados.hospAlta()).thenReturn(ALTA);
        lenient().when(estados.habDisponible()).thenReturn(DISPONIBLE);
        lenient().when(estados.habOcupada()).thenReturn(OCUPADA);
        lenient().when(estados.habMantenimiento()).thenReturn(MANTENIMIENTO);
    }

    private Habitacion habitacion(Long id, int capacidad, Long estado) {
        Habitacion h = new Habitacion();
        h.setIdHabitacion(id);
        h.setNumero("101");
        h.setCapacidad(capacidad);
        h.setIdEstadoHabitacion(estado);
        return h;
    }

    private Hospitalizaciones hospitalizacion(Long id, Long idEstado) {
        Hospitalizaciones h = new Hospitalizaciones();
        h.setIdHospitalizacion(id);
        h.setIdPaciente(1L);
        h.setIdHabitacion(5L);
        h.setIdEstadoHosp(idEstado);
        h.setFechaIngreso(LocalDateTime.now().minusDays(2));
        h.setActivo(true);
        return h;
    }

    @Test
    void registrar_iniciaActiva_yMarcaOcupadaLaHabitacionAlLlenarse() {
        HospitalizacionesDTO dto = new HospitalizacionesDTO(null, 1L, 5L, 2L, null, null, null,
                "Cirugia programada", null, null, null);
        Habitacion hab = habitacion(5L, 1, DISPONIBLE);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(new Paciente()));
        when(hospitalizacionesRepository.existsByIdPacienteAndIdEstadoHosp(1L, ACTIVA)).thenReturn(false);
        when(medicoRepository.findById(2L)).thenReturn(Optional.of(new Medico()));
        when(habitacionRepository.findById(5L)).thenReturn(Optional.of(hab));
        when(hospitalizacionesRepository.countByIdHabitacionAndIdEstadoHosp(5L, ACTIVA)).thenReturn(0L);
        when(hospitalizacionesRepository.save(any(Hospitalizaciones.class))).thenAnswer(inv -> {
            Hospitalizaciones h = inv.getArgument(0);
            h.setIdHospitalizacion(7L);
            return h;
        });

        HospitalizacionesDTO r = service.registrar(dto);

        assertThat(r.idHospitalizacion()).isEqualTo(7L);
        assertThat(r.idEstadoHosp()).isEqualTo(ACTIVA);
        assertThat(r.activo()).isTrue();
        assertThat(r.fechaIngreso()).isNotNull();
        assertThat(hab.getIdEstadoHabitacion()).isEqualTo(OCUPADA);
        verify(habitacionRepository).save(hab);
    }

    @Test
    void findById_lanzaNoEncontrado_siLaHospitalizacionNoExiste() {
        when(hospitalizacionesRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.findById(99L));
    }

    @Test
    void darAlta_lanzaNoEncontrado_siLaHospitalizacionNoExiste_yNoGuarda() {
        when(hospitalizacionesRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.darAlta(99L, null));

        verify(hospitalizacionesRepository, never()).save(any());
    }

    @Test
    void darAlta_cambiaEstadoAAlta_yLiberaLaHabitacionSiNoQuedanPacientes() {
        Hospitalizaciones h = hospitalizacion(1L, ACTIVA);
        Habitacion hab = habitacion(5L, 1, OCUPADA);
        when(hospitalizacionesRepository.findById(1L)).thenReturn(Optional.of(h));
        when(hospitalizacionesRepository.save(h)).thenReturn(h);
        when(habitacionRepository.findById(5L)).thenReturn(Optional.of(hab));
        when(hospitalizacionesRepository.countByIdHabitacionAndIdEstadoHosp(5L, ACTIVA)).thenReturn(0L);

        HospitalizacionesDTO r = service.darAlta(1L, null);

        assertThat(r.idEstadoHosp()).isEqualTo(ALTA);
        assertThat(r.fechaAlta()).isNotNull();
        assertThat(hab.getIdEstadoHabitacion()).isEqualTo(DISPONIBLE);
        verify(habitacionRepository).save(hab);
    }

    @Test
    void darAlta_noLiberaLaHabitacion_siAunQuedanPacientesActivos() {
        Hospitalizaciones h = hospitalizacion(1L, ACTIVA);
        Habitacion hab = habitacion(5L, 2, OCUPADA);
        when(hospitalizacionesRepository.findById(1L)).thenReturn(Optional.of(h));
        when(hospitalizacionesRepository.save(h)).thenReturn(h);
        when(habitacionRepository.findById(5L)).thenReturn(Optional.of(hab));
        when(hospitalizacionesRepository.countByIdHabitacionAndIdEstadoHosp(5L, ACTIVA)).thenReturn(1L);

        service.darAlta(1L, null);

        assertThat(hab.getIdEstadoHabitacion()).isEqualTo(OCUPADA);
        verify(habitacionRepository, never()).save(any());
    }

    @Test
    void eliminar_lanzaNoEncontrado_siLaHospitalizacionNoExiste_yNoGuarda() {
        when(hospitalizacionesRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(99L));

        verify(hospitalizacionesRepository, never()).save(any());
    }

    @Test
    void eliminar_haceBorradoLogico_deUnaHospitalizacionConAlta_yConservaElHistorial() {
        Hospitalizaciones h = hospitalizacion(1L, ALTA);
        when(hospitalizacionesRepository.findById(1L)).thenReturn(Optional.of(h));

        service.eliminar(1L);

        assertThat(h.getActivo()).isFalse();
        verify(hospitalizacionesRepository).save(h);
        verify(hospitalizacionesRepository, never()).deleteById(any());
    }

    @Test
    void findByPaciente_lanzaNoEncontrado_siElPacienteNoExiste() {
        when(pacienteRepository.existsById(99L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> service.findByPaciente(99L));

        verify(hospitalizacionesRepository, never()).findByIdPaciente(any());
    }

    private HospitalizacionesDTO dtoIngreso() {
        return new HospitalizacionesDTO(null, 1L, 5L, 2L, null, null, null, "Motivo", null, null, null);
    }

    private void mockIngresoValido(Habitacion hab) {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(new Paciente()));
        when(medicoRepository.findById(2L)).thenReturn(Optional.of(new Medico()));
        when(habitacionRepository.findById(5L)).thenReturn(Optional.of(hab));
    }

    @Test
    void eliminar_deUnaHospitalizacionActiva_lanzaConflicto409_yNoGuarda() {
        Hospitalizaciones h = hospitalizacion(1L, ACTIVA);
        when(hospitalizacionesRepository.findById(1L)).thenReturn(Optional.of(h));

        assertThrows(DuplicadoException.class, () -> service.eliminar(1L));

        assertThat(h.getActivo()).isTrue();
        verify(hospitalizacionesRepository, never()).save(any());
    }

    @Test
    void registrar_conPacienteYaHospitalizado_lanzaConflicto409() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(new Paciente()));
        when(hospitalizacionesRepository.existsByIdPacienteAndIdEstadoHosp(1L, ACTIVA)).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.registrar(dtoIngreso()));

        verify(hospitalizacionesRepository, never()).save(any());
    }

    @Test
    void registrar_conPacienteMedicoOHabitacionInexistentes_lanzaReglaNegocio422() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ReglaNegocioException.class, () -> service.registrar(dtoIngreso()));

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(new Paciente()));
        when(medicoRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ReglaNegocioException.class, () -> service.registrar(dtoIngreso()));

        when(medicoRepository.findById(2L)).thenReturn(Optional.of(new Medico()));
        when(habitacionRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ReglaNegocioException.class, () -> service.registrar(dtoIngreso()));
    }

    @Test
    void registrar_enHabitacionEnMantenimientoOLlena_lanzaReglaNegocio422() {
        mockIngresoValido(habitacion(5L, 1, MANTENIMIENTO));
        assertThrows(ReglaNegocioException.class, () -> service.registrar(dtoIngreso()));

        mockIngresoValido(habitacion(5L, 1, OCUPADA));
        when(hospitalizacionesRepository.countByIdHabitacionAndIdEstadoHosp(5L, ACTIVA)).thenReturn(1L);
        assertThrows(ReglaNegocioException.class, () -> service.registrar(dtoIngreso()));

        verify(hospitalizacionesRepository, never()).save(any());
    }

    @Test
    void registrar_conFechaDeIngresoFutura_lanzaReglaNegocio422() {
        mockIngresoValido(habitacion(5L, 1, DISPONIBLE));
        HospitalizacionesDTO dto = new HospitalizacionesDTO(null, 1L, 5L, 2L, null, LocalDateTime.now().plusDays(1), null,
                "Motivo", null, null, null);

        assertThrows(ReglaNegocioException.class, () -> service.registrar(dto));
    }

    @Test
    void registrar_conClinicaInexistente_lanzaReglaNegocio422() {
        mockIngresoValido(habitacion(5L, 2, DISPONIBLE));
        when(clinicasRepository.findById(9L)).thenReturn(Optional.empty());
        HospitalizacionesDTO dto = new HospitalizacionesDTO(null, 1L, 5L, 2L, 9L, null, null, "Motivo", null, null, null);

        assertThrows(ReglaNegocioException.class, () -> service.registrar(dto));
    }

    @Test
    void darAlta_repetida_lanzaConflicto409() {
        Hospitalizaciones h = hospitalizacion(1L, ALTA);
        when(hospitalizacionesRepository.findById(1L)).thenReturn(Optional.of(h));

        assertThrows(DuplicadoException.class, () -> service.darAlta(1L, null));

        verify(hospitalizacionesRepository, never()).save(any());
    }

    @Test
    void darAlta_anteriorAlIngreso_lanzaReglaNegocio422() {
        Hospitalizaciones h = hospitalizacion(1L, ACTIVA);
        when(hospitalizacionesRepository.findById(1L)).thenReturn(Optional.of(h));

        assertThrows(ReglaNegocioException.class, () -> service.darAlta(1L, h.getFechaIngreso().minusDays(1)));
    }

    @Test
    void actualizar_conNullEnClinicaYDiagnostico_losBorra() {
        Hospitalizaciones h = hospitalizacion(1L, ACTIVA);
        h.setIdMedicoResponsable(2L);
        h.setIdClinica(4L);
        h.setDiagnosticoIngreso("Neumonia");
        when(hospitalizacionesRepository.findById(1L)).thenReturn(Optional.of(h));
        when(hospitalizacionesRepository.save(h)).thenReturn(h);
        HospitalizacionesDTO dto = new HospitalizacionesDTO(1L, 1L, 5L, 2L, null, null, null, " Nuevo motivo ", null, null, null);

        HospitalizacionesDTO r = service.actualizar(dto, 1L);

        assertThat(r.idClinica()).isNull();
        assertThat(r.diagnosticoIngreso()).isNull();
        assertThat(r.motivoIngreso()).isEqualTo("Nuevo motivo");
        // No cambia el ingreso ni el estado
        assertThat(h.getFechaIngreso()).isNotNull();
        assertThat(h.getIdEstadoHosp()).isEqualTo(ACTIVA);
    }

    @Test
    void actualizar_conClinicaNueva_laValidaYLaAsigna() {
        Hospitalizaciones h = hospitalizacion(1L, ACTIVA);
        h.setIdMedicoResponsable(2L);
        when(hospitalizacionesRepository.findById(1L)).thenReturn(Optional.of(h));
        when(hospitalizacionesRepository.save(h)).thenReturn(h);
        when(clinicasRepository.findById(4L)).thenReturn(Optional.of(new Clinicas()));
        HospitalizacionesDTO dto = new HospitalizacionesDTO(1L, 1L, 5L, 2L, 4L, null, null, "Motivo", "Dx", null, null);

        assertThat(service.actualizar(dto, 1L).idClinica()).isEqualTo(4L);
    }

    @Test
    void actualizar_deUnaHospitalizacionConAlta_oCambiandoElPaciente_lanzaReglaNegocio422() {
        Hospitalizaciones conAlta = hospitalizacion(1L, ALTA);
        when(hospitalizacionesRepository.findById(1L)).thenReturn(Optional.of(conAlta));
        assertThrows(ReglaNegocioException.class, () -> service.actualizar(dtoIngreso(), 1L));

        Hospitalizaciones activa = hospitalizacion(2L, ACTIVA);
        when(hospitalizacionesRepository.findById(2L)).thenReturn(Optional.of(activa));
        HospitalizacionesDTO otroPaciente = new HospitalizacionesDTO(2L, 99L, 5L, 2L, null, null, null, "Motivo", null, null, null);
        assertThrows(ReglaNegocioException.class, () -> service.actualizar(otroPaciente, 2L));
    }

    @Test
    void resumenPorPaciente_devuelveDatosMinimos_recientesPrimero_sinMotivoNiDiagnostico() {
        when(pacienteRepository.existsById(1L)).thenReturn(true);
        Hospitalizaciones vieja = new Hospitalizaciones();
        vieja.setIdHospitalizacion(1L);
        vieja.setIdPaciente(1L);
        vieja.setIdHabitacion(5L);
        vieja.setFechaIngreso(LocalDateTime.of(2026, 1, 1, 8, 0));
        vieja.setFechaAlta(LocalDateTime.of(2026, 1, 5, 8, 0));
        vieja.setIdEstadoHosp(2L);
        vieja.setMotivoIngreso("secreto");
        vieja.setDiagnosticoIngreso("secreto");
        vieja.setActivo(true);
        Hospitalizaciones reciente = new Hospitalizaciones();
        reciente.setIdHospitalizacion(2L);
        reciente.setIdPaciente(1L);
        reciente.setIdHabitacion(5L);
        reciente.setFechaIngreso(LocalDateTime.of(2026, 10, 1, 8, 0));
        reciente.setIdEstadoHosp(1L);
        reciente.setActivo(true);
        Hospitalizaciones anulada = new Hospitalizaciones();
        anulada.setIdHospitalizacion(3L);
        anulada.setActivo(false);
        when(hospitalizacionesRepository.findByIdPaciente(1L)).thenReturn(java.util.List.of(vieja, reciente, anulada));
        EstadosHospitalizacion activa = new EstadosHospitalizacion();
        activa.setDescripcion("Activa");
        EstadosHospitalizacion alta = new EstadosHospitalizacion();
        alta.setDescripcion("Alta");
        when(estadosHospitalizacionRepository.findById(1L)).thenReturn(Optional.of(activa));
        when(estadosHospitalizacionRepository.findById(2L)).thenReturn(Optional.of(alta));
        Habitacion hab = new Habitacion();
        hab.setNumero("204");
        when(habitacionRepository.findById(5L)).thenReturn(Optional.of(hab));

        java.util.List<HospitalizacionResumenDTO> r = service.resumenPorPaciente(1L);

        assertThat(r).extracting(HospitalizacionResumenDTO::idHospitalizacion).containsExactly(2L, 1L);
        assertThat(r.get(0).estado()).isEqualTo("Activa");
        assertThat(r.get(0).numeroHabitacion()).isEqualTo("204");
        assertThat(r.get(1).fechaAlta()).isEqualTo(LocalDateTime.of(2026, 1, 5, 8, 0));
        assertThat(HospitalizacionResumenDTO.class.getRecordComponents())
                .extracting(c -> c.getName())
                .containsExactly("idHospitalizacion", "fechaIngreso", "fechaAlta", "estado", "numeroHabitacion");
    }

    @Test
    void resumenPorPaciente_lanza404_siElPacienteNoExiste() {
        when(pacienteRepository.existsById(99L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> service.resumenPorPaciente(99L));
    }
}
