package com.example.hospital.asignacionenfermeros;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.asignacionenfermeros.dto.AsignacionEnfermerosDTO;
import com.example.hospital.asignacionenfermeros.impl.AsignacionEnfermerosServiceImpl;
import com.example.hospital.enfermeros.Enfermeros;
import com.example.hospital.enfermeros.EnfermerosRepository;
import com.example.hospital.hospitalizaciones.Hospitalizaciones;
import com.example.hospital.hospitalizaciones.HospitalizacionesRepository;
import com.example.hospital.shared.catalogos.EstadosCatalogo;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.turnos.Turnos;
import com.example.hospital.turnos.TurnosRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AsignacionEnfermerosServiceImplTest {

    private static final Long ACTIVA = 1L;
    private static final Long ALTA = 2L;

    @Mock AsignacionEnfermerosRepository asignacionRepository;
    @Mock EnfermerosRepository enfermerosRepository;
    @Mock HospitalizacionesRepository hospitalizacionesRepository;
    @Mock TurnosRepository turnosRepository;
    @Mock EstadosCatalogo estados;
    @InjectMocks AsignacionEnfermerosServiceImpl service;

    @BeforeEach
    void catalogo() {
        lenient().when(estados.hospActiva()).thenReturn(ACTIVA);
    }

    private Hospitalizaciones hospitalizacion(Long estado, LocalDateTime alta) {
        Hospitalizaciones h = new Hospitalizaciones();
        h.setIdHospitalizacion(1L);
        h.setIdEstadoHosp(estado);
        h.setFechaIngreso(LocalDateTime.now().minusDays(2));
        h.setFechaAlta(alta);
        return h;
    }

    private AsignacionEnfermerosDTO dto(LocalDate fecha) {
        return new AsignacionEnfermerosDTO(null, 1L, 1L, fecha, 1L, null);
    }

    private void mockEnfermeroYTurno() {
        Enfermeros e = new Enfermeros();
        e.setActivo(true);
        when(enfermerosRepository.findById(1L)).thenReturn(Optional.of(e));
    }

    private void mockTurno() {
        Turnos t = new Turnos();
        t.setActivo(true);
        when(turnosRepository.findById(1L)).thenReturn(Optional.of(t));
    }

    @Test
    void crear_valida_guardaLaAsignacionActiva() {
        mockEnfermeroYTurno();
        mockTurno();
        when(hospitalizacionesRepository.findById(1L)).thenReturn(Optional.of(hospitalizacion(ACTIVA, null)));
        when(asignacionRepository.save(any(AsignacionEnfermeros.class))).thenAnswer(inv -> inv.getArgument(0));

        AsignacionEnfermerosDTO r = service.crear(dto(LocalDate.now()));

        assertThat(r.activo()).isTrue();
    }

    @Test
    void crear_conEnfermeroOHospitalizacionInexistente_lanzaReglaNegocio422() {
        when(enfermerosRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(LocalDate.now())));

        mockEnfermeroYTurno();
        when(hospitalizacionesRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(LocalDate.now())));
    }

    @Test
    void crear_enUnaHospitalizacionConAlta_lanzaReglaNegocio422_conUnaSolaComprobacionDeEstado() {
        mockEnfermeroYTurno();
        // Una hospitalización con alta tiene fechaAlta Y estado Alta; basta con el estado
        when(hospitalizacionesRepository.findById(1L)).thenReturn(Optional.of(hospitalizacion(ALTA, LocalDateTime.now().minusDays(1))));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(LocalDate.now())));

        verify(asignacionRepository, never()).save(any());
    }

    @Test
    void crear_conFechaAnteriorAlIngreso_lanzaReglaNegocio422() {
        mockEnfermeroYTurno();
        mockTurno();
        when(hospitalizacionesRepository.findById(1L)).thenReturn(Optional.of(hospitalizacion(ACTIVA, null)));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(LocalDate.now().minusDays(5))));
    }

    @Test
    void crear_repetida_lanzaConflicto409() {
        mockEnfermeroYTurno();
        mockTurno();
        LocalDate hoy = LocalDate.now();
        when(hospitalizacionesRepository.findById(1L)).thenReturn(Optional.of(hospitalizacion(ACTIVA, null)));
        when(asignacionRepository.existsByIdEnfermeroAndIdHospitalizacionAndFechaAsignacionAndIdTurno(1L, 1L, hoy, 1L)).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.crear(dto(hoy)));
    }

    @Test
    void findByHospitalizacion_soloActivas_omiteLasQuitadas_ySinElFiltroLasIncluye() {
        AsignacionEnfermeros activa = new AsignacionEnfermeros();
        activa.setIdAsignacion(1L);
        activa.setActivo(true);
        AsignacionEnfermeros quitada = new AsignacionEnfermeros();
        quitada.setIdAsignacion(2L);
        quitada.setActivo(false);
        when(hospitalizacionesRepository.existsById(1L)).thenReturn(true);
        when(asignacionRepository.findByIdHospitalizacion(1L)).thenReturn(List.of(activa, quitada));

        assertThat(service.findByHospitalizacion(1L, true)).extracting(AsignacionEnfermerosDTO::idAsignacion).containsExactly(1L);
        assertThat(service.findByHospitalizacion(1L, false)).hasSize(2);
    }

    @Test
    void findByHospitalizacion_lanzaNoEncontrado_siNoExiste() {
        when(hospitalizacionesRepository.existsById(9L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> service.findByHospitalizacion(9L, false));
    }
}
