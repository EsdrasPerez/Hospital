package com.example.hospital.administracion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.estadocita.EstadoCita;
import com.example.hospital.estadocita.EstadoCitaRepository;
import com.example.hospital.estadocita.dto.EstadoCitaDTO;
import com.example.hospital.estadocita.impl.EstadoCitaServiceImpl;
import com.example.hospital.estadohabitacion.EstadoHabitacion;
import com.example.hospital.estadohabitacion.EstadoHabitacionRepository;
import com.example.hospital.estadohabitacion.dto.EstadoHabitacionDTO;
import com.example.hospital.estadohabitacion.impl.EstadoHabitacionServiceImpl;
import com.example.hospital.especialidadmedico.EspecialidadMedico;
import com.example.hospital.especialidadmedico.EspecialidadMedicoRepository;
import com.example.hospital.especialidadmedico.dto.EspecialidadMedicoDTO;
import com.example.hospital.especialidadmedico.impl.EspecialidadMedicoServiceImpl;
import com.example.hospital.medicoespecialidades.MedicoEspecialidadesRepository;
import com.example.hospital.medico.MedicoRepository;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.turnos.TurnosRepository;
import com.example.hospital.turnos.dto.TurnosDTO;
import com.example.hospital.turnos.impl.TurnosServiceImpl;
import com.example.hospital.asignacionenfermeros.AsignacionEnfermerosRepository;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OtrosCatalogosAdministracionTest {

    @Mock EstadoCitaRepository estadoCitaRepository;
    @Mock EstadoHabitacionRepository estadoHabitacionRepository;
    @Mock EspecialidadMedicoRepository especialidadMedicoRepository;
    @Mock MedicoEspecialidadesRepository medicoEspecialidadesRepository;
    @Mock MedicoRepository medicoRepository;
    @Mock TurnosRepository turnosRepository;
    @Mock AsignacionEnfermerosRepository asignacionEnfermerosRepository;
    @InjectMocks EstadoCitaServiceImpl estadosCita;
    @InjectMocks EstadoHabitacionServiceImpl estadosHabitacion;
    @InjectMocks EspecialidadMedicoServiceImpl especialidades;
    @InjectMocks TurnosServiceImpl turnos;

    // ---------- estado de cita ----------

    @Test
    void estadoCita_sinNombre_secopiaDeLaDescripcion() {
        when(estadoCitaRepository.save(any(EstadoCita.class))).thenAnswer(inv -> inv.getArgument(0));

        EstadoCitaDTO r = estadosCita.crear(new EstadoCitaDTO(null, null, "Programada", null));

        assertThat(r.nombre()).isEqualTo("Programada");
        assertThat(r.descripcion()).isEqualTo("Programada");
        assertThat(r.activo()).isTrue();
    }

    @Test
    void estadoCita_nombreRepetido409_yNombreMuyLargo422() {
        when(estadoCitaRepository.existsByNombreIgnoreCase("Otro")).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> estadosCita.crear(new EstadoCitaDTO(null, "Otro", "Confirmada", null)));
        assertThrows(ReglaNegocioException.class, () -> estadosCita.crear(new EstadoCitaDTO(null, "n".repeat(31), "Atendida", null)));

        verify(estadoCitaRepository, never()).save(any());
    }

    @Test
    void estadoCita_descripcionRepetida409() {
        when(estadoCitaRepository.existsByDescripcionIgnoreCase("Programada")).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> estadosCita.crear(new EstadoCitaDTO(null, null, "Programada", null)));
    }

    // ---------- estado de habitacion ----------

    @Test
    void estadoHabitacion_sinNombre_secopiaDeLaDescripcion_yNombreRepetido409() {
        when(estadoHabitacionRepository.save(any(EstadoHabitacion.class))).thenAnswer(inv -> inv.getArgument(0));

        EstadoHabitacionDTO r = estadosHabitacion.crear(new EstadoHabitacionDTO(null, null, "Disponible", null));
        assertThat(r.nombre()).isEqualTo("Disponible");
        assertThat(r.activo()).isTrue();

        when(estadoHabitacionRepository.existsByNombreIgnoreCase("Dup")).thenReturn(true);
        assertThrows(DuplicadoException.class, () -> estadosHabitacion.crear(new EstadoHabitacionDTO(null, "Dup", "Ocupada", null)));
    }

    // ---------- especialidades medicas ----------

    @Test
    void especialidadMedica_naceActiva_yElDuplicadoEs409() {
        when(especialidadMedicoRepository.save(any(EspecialidadMedico.class))).thenAnswer(inv -> inv.getArgument(0));
        assertThat(especialidades.crear(new EspecialidadMedicoDTO(null, "Cardiologia", null, null)).activo()).isTrue();

        when(especialidadMedicoRepository.existsByNombreIgnoreCase("Cardiologia")).thenReturn(true);
        assertThrows(DuplicadoException.class, () -> especialidades.crear(new EspecialidadMedicoDTO(null, "Cardiologia", null, null)));
    }

    // ---------- turnos ----------

    @Test
    void turno_naceActivo_descripcionRepetida409_horasIgualesYFaltantes422() {
        when(turnosRepository.save(any(com.example.hospital.turnos.Turnos.class))).thenAnswer(inv -> inv.getArgument(0));
        assertThat(turnos.registrar(new TurnosDTO(null, "Tarde", LocalTime.of(14, 0), LocalTime.of(20, 0), null)).activo()).isTrue();

        when(turnosRepository.existsByDescripcionIgnoreCase("Tarde")).thenReturn(true);
        assertThrows(DuplicadoException.class, () -> turnos.registrar(new TurnosDTO(null, "Tarde", LocalTime.of(14, 0), LocalTime.of(20, 0), null)));
        assertThrows(ReglaNegocioException.class, () -> turnos.registrar(new TurnosDTO(null, "Igual", LocalTime.NOON, LocalTime.NOON, null)));
    }
}
