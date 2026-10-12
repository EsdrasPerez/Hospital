package com.example.hospital.tratamiento.impl;

import com.example.hospital.diagnosticos.Diagnosticos;
import com.example.hospital.diagnosticos.DiagnosticosRepository;
import com.example.hospital.tratamientomedicamento.TratamientoMedicamentoRepository;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.tratamiento.Tratamiento;
import com.example.hospital.tratamiento.TratamientoRepository;
import com.example.hospital.tratamiento.TratamientoService;
import com.example.hospital.tratamiento.dto.TratamientoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TratamientoServiceImpl implements TratamientoService {

    private final TratamientoRepository tratamientoRepository;
    private final DiagnosticosRepository diagnosticosRepository;
    private final TratamientoMedicamentoRepository tratamientoMedicamentoRepository;

    public TratamientoServiceImpl(TratamientoRepository tratamientoRepository,
            DiagnosticosRepository diagnosticosRepository,
            TratamientoMedicamentoRepository tratamientoMedicamentoRepository) {
        this.tratamientoRepository = tratamientoRepository;
        this.diagnosticosRepository = diagnosticosRepository;
        this.tratamientoMedicamentoRepository = tratamientoMedicamentoRepository;
    }

    private TratamientoDTO mapToDTO(Tratamiento t) {
        return new TratamientoDTO(
                t.getIdTratamiento(),
                t.getDescripcion(),
                t.getDuracionDias(),
                t.getIndicaciones(),
                t.getFechaInicio(),
                t.getFechaFin(),
                t.getIdDiagnostico(),
                t.getActivo());
    }

    private void validarReglasDeNegocio(TratamientoDTO dto) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos del tratamiento son obligatorios.");
        }

        // RN-01: Descripción obligatoria
        if (dto.descripcion() == null || dto.descripcion().trim().isEmpty()) {
            throw new ReglaNegocioException("La descripción del tratamiento es obligatoria.");
        }

        // RN-02: Diagnóstico obligatorio y debe existir activo
        if (dto.idDiagnostico() == null) {
            throw new ReglaNegocioException("El ID del diagnóstico asociado es obligatorio.");
        }

        Diagnosticos diagnostico = diagnosticosRepository.findById(dto.idDiagnostico())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El diagnóstico con ID " + dto.idDiagnostico() + " no existe."));

        if (!Boolean.TRUE.equals(diagnostico.getActivo())) {
            throw new ReglaNegocioException("El diagnóstico con ID " + dto.idDiagnostico() + " se encuentra inactivo.");
        }

        // RN-03: Duración en días válida
        if (dto.duracionDias() != null && dto.duracionDias() <= 0) {
            throw new ReglaNegocioException("La duración del tratamiento en días debe ser mayor a cero.");
        }

        // RN-04: Coherencia de fechas
        if (dto.fechaInicio() != null && dto.fechaFin() != null) {
            if (dto.fechaFin().isBefore(dto.fechaInicio())) {
                throw new ReglaNegocioException(
                        "La fecha de fin no puede ser anterior a la fecha de inicio del tratamiento.");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TratamientoDTO> findAll() {
        return tratamientoRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TratamientoDTO> findActivos() {
        return tratamientoRepository.findByActivoTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TratamientoDTO findById(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID del tratamiento no puede ser nulo.");
        }
        Tratamiento entity = tratamientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tratamiento con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TratamientoDTO> findByDiagnostico(Long idDiagnostico) {
        if (idDiagnostico == null) {
            throw new ReglaNegocioException("El ID del diagnóstico no puede ser nulo.");
        }
        return tratamientoRepository.findByIdDiagnosticoAndActivoTrue(idDiagnostico)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TratamientoDTO crear(TratamientoDTO dto) {
        validarReglasDeNegocio(dto);

        Tratamiento entity = new Tratamiento();
        entity.setDescripcion(dto.descripcion().trim());
        entity.setDuracionDias(dto.duracionDias());
        entity.setIndicaciones(dto.indicaciones() != null ? dto.indicaciones().trim() : null);
        entity.setFechaInicio(dto.fechaInicio() != null ? dto.fechaInicio() : LocalDate.now());
        entity.setFechaFin(dto.fechaFin());
        entity.setIdDiagnostico(dto.idDiagnostico());
        entity.setActivo(dto.activo() != null ? dto.activo() : true);

        Tratamiento guardado = tratamientoRepository.save(entity);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public TratamientoDTO actualizar(TratamientoDTO dto, Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID del tratamiento es obligatorio para actualizar.");
        }

        Tratamiento existente = tratamientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tratamiento con ID: " + id));

        validarReglasDeNegocio(dto);

        existente.setDescripcion(dto.descripcion().trim());
        existente.setDuracionDias(dto.duracionDias());
        existente.setIndicaciones(dto.indicaciones() != null ? dto.indicaciones().trim() : null);
        if (dto.fechaInicio() != null) {
            existente.setFechaInicio(dto.fechaInicio());
        }
        existente.setFechaFin(dto.fechaFin());
        existente.setIdDiagnostico(dto.idDiagnostico());
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        Tratamiento actualizado = tratamientoRepository.save(existente);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID del tratamiento no puede ser nulo.");
        }

        Tratamiento existente = tratamientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tratamiento con ID: " + id));

        // Validación de integridad: no eliminar si tiene medicamentos asociados
        if (tratamientoMedicamentoRepository.existsByIdTratamiento(id)) {
            throw new ReglaNegocioException("No se puede eliminar el tratamiento porque tiene medicamentos asociados.");
        }

        // Borrado lógico sistemático
        existente.setActivo(false);
        tratamientoRepository.save(existente);
    }
}
