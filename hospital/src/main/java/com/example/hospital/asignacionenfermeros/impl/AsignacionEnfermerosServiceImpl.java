package com.example.hospital.asignacionenfermeros.impl;

import com.example.hospital.shared.catalogos.EstadosCatalogo;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
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

    private final AsignacionEnfermerosRepository asignacionEnfermerosRepository;
    private final com.example.hospital.enfermeros.EnfermerosRepository enfermerosRepository;
    private final HospitalizacionesRepository hospitalizacionesRepository;
    private final TurnosRepository turnosRepository;
    private final EstadosCatalogo estados;

    public AsignacionEnfermerosServiceImpl(
            AsignacionEnfermerosRepository asignacionEnfermerosRepository,
            com.example.hospital.enfermeros.EnfermerosRepository enfermerosRepository,
            HospitalizacionesRepository hospitalizacionesRepository,
            TurnosRepository turnosRepository,
            EstadosCatalogo estados) {
        this.asignacionEnfermerosRepository = asignacionEnfermerosRepository;
        this.enfermerosRepository = enfermerosRepository;
        this.hospitalizacionesRepository = hospitalizacionesRepository;
        this.turnosRepository = turnosRepository;
        this.estados = estados;
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
            throw new ReglaNegocioException("Los datos de asignación son requeridos");
        }
        // Los datos obligatorios (enfermero, hospitalización, turno y fecha) los valida Bean Validation en el DTO.

        // Regla 1 y 2: El enfermero debe existir y estar activo (referencia del cuerpo: 422)
        Enfermeros enfermero = enfermerosRepository.findById(dto.idEnfermero())
                .orElseThrow(() -> new ReglaNegocioException("El enfermero con ID " + dto.idEnfermero() + " no existe"));
        if (Boolean.FALSE.equals(enfermero.getActivo())) {
            throw new ReglaNegocioException("El enfermero no se encuentra activo");
        }

        // Regla 3: La hospitalización debe existir
        Hospitalizaciones hospitalizacion = hospitalizacionesRepository.findById(dto.idHospitalizacion())
                .orElseThrow(() -> new ReglaNegocioException("La hospitalización con ID " + dto.idHospitalizacion() + " no existe"));

        // Regla 4: La hospitalización debe estar Activa. Esta única comprobación basta: el alta
        // (HospitalizacionesService.darAlta) establece fechaAlta y cambia el estado a Alta en la misma
        // operación, así que toda hospitalización con fechaAlta ya no está Activa. La comprobación anterior
        // sobre fechaAlta (ya finalizada) era redundante y además ignoraba las altas de hoy.
        if (hospitalizacion.getIdEstadoHosp() != null && !estados.hospActiva().equals(hospitalizacion.getIdEstadoHosp())) {
            throw new ReglaNegocioException("La hospitalización no se encuentra en estado Activa");
        }

        // Regla 5: El turno seleccionado debe existir y estar activo
        Turnos turno = turnosRepository.findById(dto.idTurno())
                .orElseThrow(() -> new ReglaNegocioException("El turno con ID " + dto.idTurno() + " no existe"));
        if (Boolean.FALSE.equals(turno.getActivo())) {
            throw new ReglaNegocioException("El turno seleccionado no se encuentra activo");
        }

        // Regla 6: La fecha de asignación no puede ser anterior al ingreso del paciente
        if (hospitalizacion.getFechaIngreso() != null) {
            LocalDate fechaIngreso = hospitalizacion.getFechaIngreso().toLocalDate();
            if (dto.fechaAsignacion().isBefore(fechaIngreso)) {
                throw new ReglaNegocioException("La fecha de asignación (" + dto.fechaAsignacion() +
                        ") no puede ser anterior a la fecha de ingreso del paciente (" + fechaIngreso + ")");
            }
        }

        // Regla 9: No se deben permitir asignaciones posteriores al alta hospitalaria
        if (hospitalizacion.getFechaAlta() != null) {
            LocalDate fechaAlta = hospitalizacion.getFechaAlta().toLocalDate();
            if (dto.fechaAsignacion().isAfter(fechaAlta)) {
                throw new ReglaNegocioException("No se permiten asignaciones posteriores al alta hospitalaria (" + fechaAlta + ")");
            }
        }

        // Regla 7: No debe existir una asignación idéntica para el mismo enfermero, hospitalización, fecha y turno (409)
        // Regla 8: Se permite que el enfermero atienda a otros pacientes en el mismo turno
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
            throw new DuplicadoException("Ya existe una asignación idéntica para este enfermero en la misma hospitalización, fecha y turno");
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
            throw new ReglaNegocioException("El ID de asignación es obligatorio");
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
            throw new ReglaNegocioException("El ID de asignación es obligatorio");
        }

        AsignacionEnfermeros existente = asignacionEnfermerosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la asignación con ID: " + id));

        // Eliminación lógica
        existente.setActivo(false);
        asignacionEnfermerosRepository.save(existente);
    }

    @Override
    public List<AsignacionEnfermerosDTO> findByEnfermero(Long idEnfermero) {
        if (!enfermerosRepository.existsById(idEnfermero)) {
            throw new RecursoNoEncontradoException("No existe el enfermero con ID: " + idEnfermero);
        }
        return asignacionEnfermerosRepository.findByIdEnfermero(idEnfermero)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AsignacionEnfermerosDTO> findByHospitalizacion(Long idHospitalizacion, boolean soloActivas) {
        if (!hospitalizacionesRepository.existsById(idHospitalizacion)) {
            throw new RecursoNoEncontradoException("No existe la hospitalización con ID: " + idHospitalizacion);
        }
        return asignacionEnfermerosRepository.findByIdHospitalizacion(idHospitalizacion)
                .stream()
                .filter(a -> !soloActivas || Boolean.TRUE.equals(a.getActivo()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}
