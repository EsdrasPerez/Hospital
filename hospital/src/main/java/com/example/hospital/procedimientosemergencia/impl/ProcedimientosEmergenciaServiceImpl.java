package com.example.hospital.procedimientosemergencia.impl;

import com.example.hospital.procedimientosemergencia.ProcedimientosEmergencia;
import com.example.hospital.procedimientosemergencia.ProcedimientosEmergenciaRepository;
import com.example.hospital.procedimientosemergencia.ProcedimientosEmergenciaService;
import com.example.hospital.procedimientosemergencia.dto.ProcedimientosEmergenciaDTO;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.emergencia.Emergencia;
import com.example.hospital.emergencia.EmergenciaRepository;
import com.example.hospital.medico.Medico;
import com.example.hospital.medico.MedicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcedimientosEmergenciaServiceImpl implements ProcedimientosEmergenciaService {

    private final ProcedimientosEmergenciaRepository procedimientosEmergenciaRepository;
    private final EmergenciaRepository emergenciaRepository;
    private final MedicoRepository medicoRepository;

    public ProcedimientosEmergenciaServiceImpl(ProcedimientosEmergenciaRepository procedimientosEmergenciaRepository,
                                             EmergenciaRepository emergenciaRepository,
                                             MedicoRepository medicoRepository) {
        this.procedimientosEmergenciaRepository = procedimientosEmergenciaRepository;
        this.emergenciaRepository = emergenciaRepository;
        this.medicoRepository = medicoRepository;
    }

    private ProcedimientosEmergenciaDTO mapToDTO(ProcedimientosEmergencia e) {
        return new ProcedimientosEmergenciaDTO(
                e.getIdProcedimiento(),
                e.getIdEmergencia(),
                e.getDescripcion(),
                e.getFechaHora(),
                e.getIdMedico(),
                e.getObservaciones(),
                e.getActivo()
        );
    }

    private void validar(ProcedimientosEmergenciaDTO dto, Long idActual) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos de el procedimiento son requeridos");
        }
        // Campos obligatorios y tamaños: Bean Validation en ProcedimientosEmergenciaDTO (400 con detalles).
        // Referencias inexistentes o inactivas en el cuerpo: 422.
        Emergencia emergencia = emergenciaRepository.findById(dto.idEmergencia())
                .orElseThrow(() -> new ReglaNegocioException("No existe la emergencia con ID: " + dto.idEmergencia()));
        if (emergencia.getFechaHoraAlta() != null && idActual == null) {
            throw new ReglaNegocioException("No se pueden registrar procedimientos en una emergencia cerrada");
        }
        Medico medico = medicoRepository.findById(dto.idMedico())
                .orElseThrow(() -> new ReglaNegocioException("El médico con ID " + dto.idMedico() + " no existe"));
        if (!Boolean.TRUE.equals(medico.getEstado())) {
            throw new ReglaNegocioException("El médico con ID " + dto.idMedico() + " se encuentra inactivo");
        }
        if (dto.fechaHora() != null) {
            if (dto.fechaHora().isAfter(java.time.LocalDateTime.now())) {
                throw new ReglaNegocioException("La fecha del procedimiento no puede ser futura");
            }
            if (emergencia.getFechaHoraIngreso() != null && dto.fechaHora().isBefore(emergencia.getFechaHoraIngreso())) {
                throw new ReglaNegocioException("El procedimiento no puede ser anterior al ingreso a emergencia");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcedimientosEmergenciaDTO> findAll() {
        return procedimientosEmergenciaRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProcedimientosEmergenciaDTO findById(Long id) {
        return mapToDTO(buscar(id));
    }

    @Override
    @Transactional
    public ProcedimientosEmergenciaDTO crear(ProcedimientosEmergenciaDTO dto) {
        validar(dto, null);
        ProcedimientosEmergencia entity = new ProcedimientosEmergencia();
        entity.setIdEmergencia(dto.idEmergencia());
        entity.setDescripcion(dto.descripcion().trim());
        entity.setFechaHora(dto.fechaHora() != null ? dto.fechaHora() : java.time.LocalDateTime.now());
        entity.setIdMedico(dto.idMedico());
        entity.setObservaciones(dto.observaciones());
        entity.setActivo(dto.activo() != null ? dto.activo() : true);
        return mapToDTO(procedimientosEmergenciaRepository.save(entity));
    }

    @Override
    @Transactional
    public ProcedimientosEmergenciaDTO actualizar(ProcedimientosEmergenciaDTO dto, Long id) {
        ProcedimientosEmergencia existente = buscar(id);
        validar(dto, id);
        ProcedimientosEmergencia entity = existente;
        entity.setIdEmergencia(dto.idEmergencia());
        entity.setDescripcion(dto.descripcion().trim());
        if (dto.fechaHora() != null) {
            entity.setFechaHora(dto.fechaHora());
        }
        entity.setIdMedico(dto.idMedico());
        entity.setObservaciones(dto.observaciones());
        if (dto.activo() != null) {
            entity.setActivo(dto.activo());
        }
        return mapToDTO(procedimientosEmergenciaRepository.save(entity));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        ProcedimientosEmergencia existente = buscar(id);
        // Eliminación lógica para conservar el historial
        existente.setActivo(false);
        procedimientosEmergenciaRepository.save(existente);
    }

    private ProcedimientosEmergencia buscar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID de el procedimiento es obligatorio");
        }
        return procedimientosEmergenciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el procedimiento con ID: " + id));
    }
}
