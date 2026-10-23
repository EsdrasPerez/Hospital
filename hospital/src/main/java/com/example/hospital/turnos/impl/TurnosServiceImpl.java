package com.example.hospital.turnos.impl;

import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.asignacionenfermeros.AsignacionEnfermerosRepository;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.turnos.Turnos;
import com.example.hospital.turnos.TurnosRepository;
import com.example.hospital.turnos.TurnosService;
import com.example.hospital.turnos.dto.TurnosDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TurnosServiceImpl implements TurnosService {

    private final TurnosRepository turnosRepository;
    private final AsignacionEnfermerosRepository asignacionEnfermerosRepository;

    public TurnosServiceImpl(TurnosRepository turnosRepository,
                             AsignacionEnfermerosRepository asignacionEnfermerosRepository) {
        this.turnosRepository = turnosRepository;
        this.asignacionEnfermerosRepository = asignacionEnfermerosRepository;
    }

    private TurnosDTO mapToDTO(Turnos t) {
        return new TurnosDTO(
                t.getIdTurno(),
                t.getDescripcion(),
                t.getHoraInicio(),
                t.getHoraFin(),
                t.getActivo()
        );
    }

    private Turnos mapToEntity(TurnosDTO dto) {
        Turnos t = new Turnos();
        t.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        t.setHoraInicio(dto.horaInicio());
        t.setHoraFin(dto.horaFin());
        t.setActivo(dto.activo() != null ? dto.activo() : true); // activo por defecto al crear
        return t;
    }

    /**
     * Valida si dos intervalos horarios se superponen.
     * Soporta turnos normales (mismo día) y turnos que cruzan la medianoche (nocturnos).
     */
    private boolean seSuperponen(LocalTime i1, LocalTime f1, LocalTime i2, LocalTime f2) {
        long inicio1Min = i1.toSecondOfDay() / 60;
        long fin1Min = f1.toSecondOfDay() / 60;
        if (fin1Min <= inicio1Min) {
            fin1Min += 24 * 60; // Cruza la medianoche
        }

        long inicio2Min = i2.toSecondOfDay() / 60;
        long fin2Min = f2.toSecondOfDay() / 60;
        if (fin2Min <= inicio2Min) {
            fin2Min += 24 * 60; // Cruza la medianoche
        }

        // Caso normal o desplazado 24 horas para cubrir ciclo diario
        return (inicio1Min < fin2Min && fin1Min > inicio2Min)
                || ((inicio1Min + 24 * 60) < (fin2Min + 24 * 60) && (fin1Min + 24 * 60) > (inicio2Min + 24 * 60));
    }

    /**
     * Valida las reglas de negocio de registro/actualización de turnos.
     */
    private void validarHorariosYDescripcion(TurnosDTO dto, Long idTurnoActual) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos del turno son obligatorios");
        }

        // RN-01: Todo turno debe tener una descripción. No permitir nulos, vacíos o espacios
        if (dto.descripcion() == null || dto.descripcion().trim().isEmpty()) {
            throw new ReglaNegocioException("La descripción del turno es obligatoria y no puede estar vacía");
        }

        String descripcionLimpia = dto.descripcion().trim();

        // RN-02 / RN-09: No pueden existir dos turnos con la misma descripción (sin distinguir mayúsculas/minúsculas)
        boolean duplicadoDescripcion;
        if (idTurnoActual == null) {
            duplicadoDescripcion = turnosRepository.existsByDescripcionIgnoreCase(descripcionLimpia);
        } else {
            duplicadoDescripcion = turnosRepository.existsByDescripcionIgnoreCaseAndIdTurnoNot(descripcionLimpia, idTurnoActual);
        }

        if (duplicadoDescripcion) {
            throw new DuplicadoException("Ya existe un turno con la descripción: " + descripcionLimpia);
        }

        // RN-03: Todo turno debe tener horaInicio y horaFin obligatorios
        if (dto.horaInicio() == null) {
            throw new ReglaNegocioException("La hora de inicio es obligatoria");
        }
        if (dto.horaFin() == null) {
            throw new ReglaNegocioException("La hora de finalización es obligatoria");
        }

        // RN-04: La hora de inicio y finalización no pueden ser iguales
        if (dto.horaInicio().equals(dto.horaFin())) {
            throw new ReglaNegocioException("La hora de inicio y la hora de finalización no pueden ser iguales");
        }

        // RN-05 y RN-06: Duración mayor a cero. Si horaFin < horaInicio, contempla cruce de medianoche (turno nocturno)
        long duracionMinutos;
        if (dto.horaFin().isAfter(dto.horaInicio())) {
            duracionMinutos = Duration.between(dto.horaInicio(), dto.horaFin()).toMinutes();
        } else {
            // Termina al día siguiente
            duracionMinutos = Duration.between(dto.horaInicio(), LocalTime.MAX).toMinutes()
                    + Duration.between(LocalTime.MIN, dto.horaFin()).toMinutes() + 1;
        }

        if (duracionMinutos <= 0) {
            throw new ReglaNegocioException("La duración del turno debe ser mayor a cero");
        }

        // RN-07: No permitir turnos con horarios superpuestos con turnos activos existentes
        List<Turnos> turnosExistentes = turnosRepository.findAll();
        for (Turnos t : turnosExistentes) {
            if (idTurnoActual != null && t.getIdTurno().equals(idTurnoActual)) {
                continue; // Ignorar el mismo turno al actualizar
            }
            if (Boolean.FALSE.equals(t.getActivo())) {
                continue;
            }

            if (seSuperponen(dto.horaInicio(), dto.horaFin(), t.getHoraInicio(), t.getHoraFin())) {
                throw new ReglaNegocioException("El horario (" + dto.horaInicio() + " - " + dto.horaFin() +
                        ") se superpone con el turno existente '" + t.getDescripcion() + "' (" +
                        t.getHoraInicio() + " - " + t.getHoraFin() + ")");
            }
        }
    }

    @Override
    public List<TurnosDTO> findAll() {
        return turnosRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TurnosDTO findById(Long id) {
        Turnos t = turnosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el turno con ID: " + id));
        return mapToDTO(t);
    }

    @Override
    @Transactional
    public TurnosDTO registrar(TurnosDTO dto) {
        validarHorariosYDescripcion(dto, null);

        Turnos entity = mapToEntity(dto);
        if (entity.getActivo() == null) {
            entity.setActivo(true);
        }

        Turnos guardado = turnosRepository.save(entity);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public TurnosDTO actualizar(TurnosDTO dto, Long id) {
        // RN-08: Solo se podrán modificar turnos existentes
        if (id == null) {
            throw new ReglaNegocioException("El ID del turno es obligatorio");
        }

        Turnos existente = turnosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el turno con ID: " + id));

        // RN-09 y RN-10: Valida descripción duplicada y nuevas validaciones de horario
        validarHorariosYDescripcion(dto, id);

        // RN-11: No se podrán modificar los horarios si el cambio afecta asignaciones de enfermeros vigentes o futuras
        boolean cambioHorario = !existente.getHoraInicio().equals(dto.horaInicio()) || !existente.getHoraFin().equals(dto.horaFin());
        if (cambioHorario) {
            boolean tieneAsignacionesFuturas = asignacionEnfermerosRepository
                    .existsByIdTurnoAndFechaAsignacionGreaterThanEqual(id, LocalDate.now());
            if (tieneAsignacionesFuturas) {
                throw new DuplicadoException("No se pueden modificar los horarios del turno porque tiene asignaciones de enfermeros vigentes o futuras asociadas");
            }
        }

        existente.setDescripcion(dto.descripcion().trim());
        existente.setHoraInicio(dto.horaInicio());
        existente.setHoraFin(dto.horaFin());
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        Turnos actualizado = turnosRepository.save(existente);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID del turno es obligatorio");
        }

        Turnos existente = turnosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el turno con ID: " + id));

        // Eliminación lógica para conservar el historial y mantener integridad referencial
        existente.setActivo(false);
        turnosRepository.save(existente);
    }
}
