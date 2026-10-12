package com.example.hospital.asignacionenfermeros.impl;

import com.example.hospital.asignacionenfermeros.AsignacionEnfermeros;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.asignacionenfermeros.AsignacionEnfermerosRepository;
import com.example.hospital.asignacionenfermeros.AsignacionEnfermerosService;
import com.example.hospital.asignacionenfermeros.dto.AsignacionEnfermerosDTO;
import com.example.hospital.enfermeros.Enfermeros;
import com.example.hospital.enfermeros.EnfermerosRepository;
import com.example.hospital.hospitalizaciones.Hospitalizaciones;
import com.example.hospital.hospitalizaciones.HospitalizacionesRepository;
import com.example.hospital.turnos.Turnos;
import com.example.hospital.turnos.TurnosRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AsignacionEnfermerosServiceImpl implements AsignacionEnfermerosService {

    // ID del estado de hospitalización activa (RN-02 / RN-10 de hospitalizaciones)
    private static final Long ESTADO_HOSP_ACTIVA = 1L;

    private final AsignacionEnfermerosRepository asignacionEnfermerosRepository;
    private final com.example.hospital.enfermeros.EnfermerosRepository enfermerosRepository;
    private final HospitalizacionesRepository hospitalizacionesRepository;
    private final TurnosRepository turnosRepository;

    public AsignacionEnfermerosServiceImpl(
            AsignacionEnfermerosRepository asignacionEnfermerosRepository,
            com.example.hospital.enfermeros.EnfermerosRepository enfermerosRepository,
            HospitalizacionesRepository hospitalizacionesRepository,
            TurnosRepository turnosRepository) {
        this.asignacionEnfermerosRepository = asignacionEnfermerosRepository;
        this.enfermerosRepository = enfermerosRepository;
        this.hospitalizacionesRepository = hospitalizacionesRepository;
        this.turnosRepository = turnosRepository;
    }

    private AsignacionEnfermerosDTO mapToDTO(AsignacionEnfermeros a) {
        return new AsignacionEnfermerosDTO(
                a.getIdAsignacion(),
                a.getIdEnfermero(),
                a.getIdHospitalizacion(),
                a.getFechaAsignacion(),
                a.getIdTurno(),
                a.getActivo()
        );
    }

    private AsignacionEnfermeros mapToEntity(AsignacionEnfermerosDTO dto) {
        AsignacionEnfermeros entity = new AsignacionEnfermeros();
        entity.setIdEnfermero(dto.idEnfermero());
        entity.setIdHospitalizacion(dto.idHospitalizacion());
        entity.setFechaAsignacion(dto.fechaAsignacion());
        entity.setIdTurno(dto.idTurno());
        entity.setActivo(dto.activo());
        return entity;
    }

    /**
     * Valida las reglas de negocio para asignación de enfermeros:
     * 1. El enfermero debe existir.
     * 2. El enfermero debe estar activo.
     * 3. La hospitalización debe existir.
     * 4. La hospitalización no debe estar finalizada (fechaAlta ya establecida o estado no activo).
     * 5. El turno seleccionado debe existir y estar activo.
     * 6. La fecha de asignación no puede ser anterior al ingreso del paciente.
     * 7. No debe existir una asignación idéntica para el mismo enfermero, hospitalización, fecha y turno.
     * 8. El enfermero puede atender a varios pacientes durante un mismo turno (permitido).
     * 9. No se deben permitir asignaciones posteriores al alta hospitalaria.
     */
    private void validarReglasDeNegocio(AsignacionEnfermerosDTO dto, Long idAsignacionActual) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de asignación son requeridos");
        }
        if (dto.idEnfermero() == null) {
            throw new IllegalArgumentException("El ID del enfermero es obligatorio");
        }
        if (dto.idHospitalizacion() == null) {
            throw new IllegalArgumentException("El ID de hospitalización es obligatorio");
        }
        if (dto.idTurno() == null) {
            throw new IllegalArgumentException("El ID de turno es obligatorio");
        }
        if (dto.fechaAsignacion() == null) {
            throw new IllegalArgumentException("La fecha de asignación es obligatoria");
        }

        // Regla 1: El enfermero debe existir
        Enfermeros enfermero = enfermerosRepository.findById(dto.idEnfermero())
                .orElseThrow(() -> new IllegalArgumentException("El enfermero con ID " + dto.idEnfermero() + " no existe"));

        // Regla 2: El enfermero debe estar activo
        if (Boolean.FALSE.equals(enfermero.getActivo())) {
            throw new IllegalArgumentException("El enfermero no se encuentra activo");
        }

        // Regla 3: La hospitalización debe existir
        Hospitalizaciones hospitalizacion = hospitalizacionesRepository.findById(dto.idHospitalizacion())
                .orElseThrow(() -> new IllegalArgumentException("La hospitalización con ID " + dto.idHospitalizacion() + " no existe"));

        // Regla 4: La hospitalización no debe estar finalizada
        if (hospitalizacion.getFechaAlta() != null && hospitalizacion.getFechaAlta().toLocalDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La hospitalización ya se encuentra finalizada");
        }
        if (hospitalizacion.getIdEstadoHosp() != null && !ESTADO_HOSP_ACTIVA.equals(hospitalizacion.getIdEstadoHosp())) {
            throw new IllegalArgumentException("La hospitalización no se encuentra en estado Activa");
        }

        // Regla 5: El turno seleccionado debe existir y estar activo
        Turnos turno = turnosRepository.findById(dto.idTurno())
                .orElseThrow(() -> new IllegalArgumentException("El turno con ID " + dto.idTurno() + " no existe"));

        if (Boolean.FALSE.equals(turno.getActivo())) {
            throw new IllegalArgumentException("El turno seleccionado no se encuentra activo");
        }

        // Regla 6: La fecha de asignación no puede ser anterior al ingreso del paciente
        if (hospitalizacion.getFechaIngreso() != null) {
            LocalDate fechaIngreso = hospitalizacion.getFechaIngreso().toLocalDate();
            if (dto.fechaAsignacion().isBefore(fechaIngreso)) {
                throw new IllegalArgumentException("La fecha de asignación (" + dto.fechaAsignacion() +
                        ") no puede ser anterior a la fecha de ingreso del paciente (" + fechaIngreso + ")");
            }
        }

        // Regla 9: No se deben permitir asignaciones posteriores al alta hospitalaria
        if (hospitalizacion.getFechaAlta() != null) {
            LocalDate fechaAlta = hospitalizacion.getFechaAlta().toLocalDate();
            if (dto.fechaAsignacion().isAfter(fechaAlta)) {
                throw new IllegalArgumentException("No se permiten asignaciones posteriores al alta hospitalaria (" + fechaAlta + ")");
            }
        }

        // Regla 7: No debe existir una asignación idéntica para el mismo enfermero, hospitalización, fecha y turno
        // Regla 8: Se permite que el enfermero atienda a otros pacientes en el mismo turno, pero no duplicar la misma asignación
        boolean existeDuplicado;
        if (idAsignacionActual == null) {
            existeDuplicado = asignacionEnfermerosRepository
                    .existsByIdEnfermeroAndIdHospitalizacionAndFechaAsignacionAndIdTurno(
                            dto.idEnfermero(), dto.idHospitalizacion(), dto.fechaAsignacion(), dto.idTurno()
                    );
        } else {
            existeDuplicado = asignacionEnfermerosRepository
                    .existsByIdEnfermeroAndIdHospitalizacionAndFechaAsignacionAndIdTurnoAndIdAsignacionNot(
                            dto.idEnfermero(), dto.idHospitalizacion(), dto.fechaAsignacion(), dto.idTurno(), idAsignacionActual
                    );
        }

        if (existeDuplicado) {
            throw new IllegalArgumentException("Ya existe una asignación idéntica para este enfermero en la misma hospitalización, fecha y turno");
        }
    }

    @Override
    public List<AsignacionEnfermerosDTO> findAll() {
        return asignacionEnfermerosRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AsignacionEnfermerosDTO findById(Long id) {
        AsignacionEnfermeros entity = asignacionEnfermerosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la asignación con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public AsignacionEnfermerosDTO crear(AsignacionEnfermerosDTO dto) {
        validarReglasDeNegocio(dto, null);

        AsignacionEnfermeros entity = mapToEntity(dto);
        if (entity.getActivo() == null) {
            entity.setActivo(true);
        }

        AsignacionEnfermeros guardado = asignacionEnfermerosRepository.save(entity);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public AsignacionEnfermerosDTO actualizar(AsignacionEnfermerosDTO dto, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de asignación es obligatorio");
        }

        AsignacionEnfermeros existente = asignacionEnfermerosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la asignación con ID: " + id));

        validarReglasDeNegocio(dto, id);

        existente.setIdEnfermero(dto.idEnfermero());
        existente.setIdHospitalizacion(dto.idHospitalizacion());
        existente.setFechaAsignacion(dto.fechaAsignacion());
        existente.setIdTurno(dto.idTurno());
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        AsignacionEnfermeros actualizado = asignacionEnfermerosRepository.save(existente);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de asignación es obligatorio");
        }

        AsignacionEnfermeros existente = asignacionEnfermerosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la asignación con ID: " + id));

        // Eliminación lógica
        existente.setActivo(false);
        asignacionEnfermerosRepository.save(existente);
    }
}
