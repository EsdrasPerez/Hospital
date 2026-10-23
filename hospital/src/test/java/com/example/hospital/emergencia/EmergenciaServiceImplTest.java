package com.example.hospital.emergencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.emergencia.dto.EmergenciaDTO;
import com.example.hospital.emergencia.impl.EmergenciaServiceImpl;
import com.example.hospital.habitacion.HabitacionRepository;
import com.example.hospital.medico.MedicoRepository;
import com.example.hospital.paciente.PacienteRepository;
import com.example.hospital.procedimientosemergencia.ProcedimientosEmergenciaRepository;
import com.example.hospital.paciente.Paciente;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.triaje.Triaje;
import com.example.hospital.triaje.TriajeRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmergenciaServiceImplTest {

    @Mock EmergenciaRepository emergenciaRepository;
    @Mock PacienteRepository pacienteRepository;
    @Mock MedicoRepository medicoRepository;
    @Mock HabitacionRepository habitacionRepository;
    @Mock TriajeRepository triajeRepository;
    @Mock ProcedimientosEmergenciaRepository procedimientosEmergenciaRepository;
    @InjectMocks EmergenciaServiceImpl service;

    private Emergencia emergencia(Long id) {
        Emergencia e = new Emergencia();
        e.setIdEmergencia(id);
        e.setMotivo("Dolor toracico");
        e.setFechaHoraIngreso(LocalDateTime.now().minusHours(3));
        e.setActivo(true);
        return e;
    }

    @Test
    void crear_conNombreTemporal_asignaFechaIngresoYActivo_yGuarda() {
        EmergenciaDTO dto = new EmergenciaDTO(null, "  Paciente NN  ", null, "  Accidente de transito  ",
                null, null, null, null, null, 2L, null);
        when(triajeRepository.findById(2L)).thenReturn(Optional.of(triaje(2L, true)));
        when(emergenciaRepository.save(any(Emergencia.class))).thenAnswer(inv -> {
            Emergencia e = inv.getArgument(0);
            e.setIdEmergencia(4L);
            return e;
        });

        EmergenciaDTO r = service.crear(dto);

        assertThat(r.idEmergencia()).isEqualTo(4L);
        assertThat(r.nombreTemporal()).isEqualTo("Paciente NN");
        assertThat(r.motivo()).isEqualTo("Accidente de transito");
        assertThat(r.fechaHoraIngreso()).isNotNull();
        assertThat(r.activo()).isTrue();
    }

    @Test
    void findById_lanzaNoEncontrado_siLaEmergenciaNoExiste() {
        when(emergenciaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.findById(99L));
    }

    @Test
    void actualizar_lanzaNoEncontrado_siLaEmergenciaNoExiste_yNoGuarda() {
        when(emergenciaRepository.findById(99L)).thenReturn(Optional.empty());
        EmergenciaDTO dto = new EmergenciaDTO(null, "NN", null, "Motivo", null, null, null, null, null, 2L, true);

        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizar(dto, 99L));

        verify(emergenciaRepository, never()).save(any());
    }

    @Test
    void darAlta_lanzaNoEncontrado_siLaEmergenciaNoExiste_yNoGuarda() {
        when(emergenciaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.darAlta(99L, null));

        verify(emergenciaRepository, never()).save(any());
    }

    @Test
    void darAlta_registraLaFechaDeAlta_enUnaEmergenciaActivaSinAlta() {
        Emergencia e = emergencia(1L);
        when(emergenciaRepository.findById(1L)).thenReturn(Optional.of(e));
        when(emergenciaRepository.save(e)).thenReturn(e);

        EmergenciaDTO r = service.darAlta(1L, null);

        assertThat(r.fechaHoraAlta()).isNotNull();
        assertThat(e.getFechaHoraAlta()).isAfter(e.getFechaHoraIngreso());
    }

    @Test
    void eliminar_lanzaNoEncontrado_siLaEmergenciaNoExiste_yNoGuarda() {
        when(emergenciaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(99L));

        verify(emergenciaRepository, never()).save(any());
    }

    @Test
    void eliminar_haceBorradoLogico_siNoTieneProcedimientosActivos() {
        Emergencia e = emergencia(1L);
        when(emergenciaRepository.findById(1L)).thenReturn(Optional.of(e));
        when(procedimientosEmergenciaRepository.existsByIdEmergenciaAndActivoTrue(1L)).thenReturn(false);

        service.eliminar(1L);

        assertThat(e.getActivo()).isFalse();
        verify(emergenciaRepository).save(e);
        verify(emergenciaRepository, never()).deleteById(any());
    }

    @Test
    void findActivas_devuelveSoloLasEmergenciasActivas() {
        when(emergenciaRepository.findByActivoTrue()).thenReturn(List.of(emergencia(1L), emergencia(2L)));

        assertThat(service.findActivas()).hasSize(2);
    }

    private Triaje triaje(Long id, boolean activo) {
        Triaje t = new Triaje();
        t.setIdTriaje(id);
        t.setNivelDeEmergencia("N" + id);
        t.setActivo(activo);
        return t;
    }

    private EmergenciaDTO dtoConTriaje(Long idPaciente, LocalDateTime alta) {
        return new EmergenciaDTO(null, "NN", null, "Motivo", null, alta, idPaciente, null, null, 2L, null);
    }

    @Test
    void crear_ignoraLaFechaDeAltaDelCuerpo_unIngresoNuevoNaceSinAlta() {
        when(triajeRepository.findById(2L)).thenReturn(Optional.of(triaje(2L, true)));
        when(emergenciaRepository.save(any(Emergencia.class))).thenAnswer(inv -> inv.getArgument(0));

        EmergenciaDTO r = service.crear(dtoConTriaje(null, LocalDateTime.now()));

        assertThat(r.fechaHoraAlta()).isNull();
    }

    @Test
    void actualizar_conservaLaFechaDeAltaExistente_aunqueElCuerpoNoLaTraiga() {
        Emergencia e = emergencia(1L);
        LocalDateTime alta = LocalDateTime.now().minusHours(1);
        e.setFechaHoraAlta(alta);
        when(emergenciaRepository.findById(1L)).thenReturn(Optional.of(e));
        when(triajeRepository.findById(2L)).thenReturn(Optional.of(triaje(2L, true)));
        when(emergenciaRepository.save(e)).thenReturn(e);

        EmergenciaDTO r = service.actualizar(dtoConTriaje(null, null), 1L);

        assertThat(r.fechaHoraAlta()).isEqualTo(alta);
        assertThat(e.getFechaHoraAlta()).isEqualTo(alta);
    }

    @Test
    void crear_rechazaUnPacienteInexistente_con422() {
        when(pacienteRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ReglaNegocioException.class, () -> service.crear(dtoConTriaje(9L, null)));

        verify(emergenciaRepository, never()).save(any());
    }

    @Test
    void crear_rechazaUnPacienteInactivo_con422() {
        Paciente p = new Paciente();
        p.setActivo(false);
        when(pacienteRepository.findById(2L)).thenReturn(Optional.of(p));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dtoConTriaje(2L, null)));
    }

    @Test
    void crear_rechazaUnTriajeInexistenteOInactivo_con422() {
        when(triajeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ReglaNegocioException.class, () -> service.crear(dtoConTriaje(null, null)));

        when(triajeRepository.findById(2L)).thenReturn(Optional.of(triaje(2L, false)));
        assertThrows(ReglaNegocioException.class, () -> service.crear(dtoConTriaje(null, null)));
    }

    @Test
    void crear_exigePacienteONombreTemporal_con422() {
        EmergenciaDTO dto = new EmergenciaDTO(null, " ", null, "Motivo", null, null, null, null, null, 2L, null);

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto));
    }

    @Test
    void darAlta_conAltaRepetida_lanzaDuplicado409() {
        Emergencia e = emergencia(1L);
        e.setFechaHoraAlta(LocalDateTime.now().minusMinutes(5));
        when(emergenciaRepository.findById(1L)).thenReturn(Optional.of(e));

        assertThrows(DuplicadoException.class, () -> service.darAlta(1L, null));

        verify(emergenciaRepository, never()).save(any());
    }

    @Test
    void darAlta_anteriorAlIngreso_lanzaReglaNegocio422() {
        Emergencia e = emergencia(1L);
        when(emergenciaRepository.findById(1L)).thenReturn(Optional.of(e));

        assertThrows(ReglaNegocioException.class, () -> service.darAlta(1L, e.getFechaHoraIngreso().minusHours(1)));
    }

    @Test
    void darAlta_deUnaEmergenciaInactiva_lanzaReglaNegocio422() {
        Emergencia e = emergencia(1L);
        e.setActivo(false);
        when(emergenciaRepository.findById(1L)).thenReturn(Optional.of(e));

        assertThrows(ReglaNegocioException.class, () -> service.darAlta(1L, null));
    }

    @Test
    void buscar_combinaPacienteYSoloActivas() {
        Emergencia a = emergencia(1L);
        a.setIdPaciente(1L);
        Emergencia b = emergencia(2L);
        b.setIdPaciente(1L);
        b.setActivo(false);
        Emergencia c = emergencia(3L);
        c.setIdPaciente(2L);
        when(emergenciaRepository.findAll()).thenReturn(List.of(a, b, c));

        assertThat(service.buscar(1L, false)).hasSize(2);
        assertThat(service.buscar(1L, true)).hasSize(1);
        assertThat(service.buscar(null, true)).hasSize(2);
    }
}
