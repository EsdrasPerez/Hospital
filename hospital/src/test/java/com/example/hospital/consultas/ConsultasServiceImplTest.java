package com.example.hospital.consultas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.citas.Citas;
import com.example.hospital.citas.CitasRepository;
import com.example.hospital.consultas.dto.ConsultasDTO;
import com.example.hospital.consultas.impl.ConsultasServiceImpl;
import com.example.hospital.diagnosticoconsulta.DiagnosticoConsultaRepository;
import com.example.hospital.estadocita.EstadoCita;
import com.example.hospital.estadocita.EstadoCitaRepository;
import com.example.hospital.estadocita.EstadoCitaService;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultasServiceImplTest {

    @Mock ConsultasRepository consultasRepository;
    @Mock CitasRepository citasRepository;
    @Mock EstadoCitaRepository estadoCitaRepository;
    @Mock DiagnosticoConsultaRepository diagnosticoConsultaRepository;
    @Mock EstadoCitaService estadoCitaService;
    @InjectMocks ConsultasServiceImpl service;

    private EstadoCita estado(Long id, String descripcion) {
        EstadoCita e = new EstadoCita();
        e.setIdEstado(id);
        e.setDescripcion(descripcion);
        e.setActivo(true);
        return e;
    }

    private Citas cita(Long idEstado, LocalDate fecha) {
        Citas c = new Citas();
        c.setIdCita(1L);
        c.setIdEstado(idEstado);
        c.setFecha(fecha);
        c.setActivo(true);
        return c;
    }

    private ConsultasDTO dto() {
        return new ConsultasDTO(null, null, "Cefalea", null, null, null, null, null, 1L, null);
    }

    private void mockEstados() {
        when(estadoCitaRepository.findByDescripcionIgnoreCase("En curso")).thenReturn(Optional.of(estado(3L, "En curso")));
        when(estadoCitaRepository.findByDescripcionIgnoreCase("Atendida")).thenReturn(Optional.of(estado(4L, "Atendida")));
    }

    @Test
    void crear_deUnaCitaConfirmada_encadenaConfirmadaEnCursoAtendida_yValidaCadaTransicion() {
        Citas cita = cita(2L, LocalDate.now());
        when(citasRepository.findById(1L)).thenReturn(Optional.of(cita));
        when(estadoCitaRepository.findById(2L)).thenReturn(Optional.of(estado(2L, "Confirmada")));
        mockEstados();
        when(consultasRepository.save(any(Consultas.class))).thenAnswer(inv -> {
            Consultas c = inv.getArgument(0);
            c.setIdConsulta(7L);
            return c;
        });

        ConsultasDTO r = service.crear(dto());

        assertThat(r.idConsulta()).isEqualTo(7L);
        assertThat(r.fechaHora()).isNotNull();
        assertThat(cita.getIdEstado()).isEqualTo(4L);
        InOrder orden = Mockito.inOrder(estadoCitaService);
        orden.verify(estadoCitaService).validarTransicion("Confirmada", "En curso");
        orden.verify(estadoCitaService).validarTransicion("En curso", "Atendida");
        verify(citasRepository).save(cita);
    }

    @Test
    void crear_deUnaCitaEnCurso_validaSoloEnCursoAtendida() {
        Citas cita = cita(3L, LocalDate.now().minusDays(1));
        when(citasRepository.findById(1L)).thenReturn(Optional.of(cita));
        when(estadoCitaRepository.findById(3L)).thenReturn(Optional.of(estado(3L, "En curso")));
        mockEstados();
        when(consultasRepository.save(any(Consultas.class))).thenAnswer(inv -> inv.getArgument(0));

        service.crear(dto());

        verify(estadoCitaService).validarTransicion("En curso", "Atendida");
        verify(estadoCitaService, never()).validarTransicion("Confirmada", "En curso");
        assertThat(cita.getIdEstado()).isEqualTo(4L);
    }

    @Test
    void crear_rechazaCitasProgramadasCanceladasOAtendidas_con422() {
        Citas cita = cita(1L, LocalDate.now());
        when(citasRepository.findById(1L)).thenReturn(Optional.of(cita));
        when(estadoCitaRepository.findById(1L)).thenReturn(Optional.of(estado(1L, "Programada")));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto()));

        verify(consultasRepository, never()).save(any());
    }

    @Test
    void crear_rechazaUnaCitaConFechaFutura_con422() {
        Citas cita = cita(2L, LocalDate.now().plusDays(1));
        when(citasRepository.findById(1L)).thenReturn(Optional.of(cita));
        when(estadoCitaRepository.findById(2L)).thenReturn(Optional.of(estado(2L, "Confirmada")));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto()));

        verify(consultasRepository, never()).save(any());
    }

    @Test
    void crear_rechazaUnaCitaInactiva_con422() {
        Citas cita = cita(2L, LocalDate.now());
        cita.setActivo(false);
        when(citasRepository.findById(1L)).thenReturn(Optional.of(cita));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto()));
    }

    @Test
    void crear_lanzaDuplicado409_siLaCitaYaTieneConsulta() {
        when(citasRepository.findById(1L)).thenReturn(Optional.of(cita(2L, LocalDate.now())));
        when(consultasRepository.existsByIdCita(1L)).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.crear(dto()));
    }

    @Test
    void listarPorCita_devuelveListaVacia_siLaCitaNoTieneConsulta() {
        when(consultasRepository.findByIdCita(5L)).thenReturn(Optional.empty());

        assertThat(service.listarPorCita(5L)).isEmpty();
    }

    @Test
    void listarPorCita_devuelveLaConsulta_siExiste() {
        Consultas c = new Consultas();
        c.setIdConsulta(9L);
        c.setIdCita(5L);
        when(consultasRepository.findByIdCita(5L)).thenReturn(Optional.of(c));

        assertThat(service.listarPorCita(5L)).hasSize(1);
    }
}
