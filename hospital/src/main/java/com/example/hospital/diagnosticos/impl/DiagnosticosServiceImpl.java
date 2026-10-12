package com.example.hospital.diagnosticos.impl;

import com.example.hospital.diagnosticoconsulta.DiagnosticoConsultaRepository;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.diagnosticos.Diagnosticos;
import com.example.hospital.diagnosticos.DiagnosticosRepository;
import com.example.hospital.diagnosticos.DiagnosticosService;
import com.example.hospital.diagnosticos.dto.DiagnosticosDTO;
import com.example.hospital.tiposdiagnostico.TiposDiagnostico;
import com.example.hospital.tiposdiagnostico.TiposDiagnosticoRepository;
import com.example.hospital.tratamiento.TratamientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiagnosticosServiceImpl implements DiagnosticosService {

    private final DiagnosticosRepository diagnosticosRepository;
    private final TiposDiagnosticoRepository tiposDiagnosticoRepository;
    private final DiagnosticoConsultaRepository diagnosticoConsultaRepository;
    private final TratamientoRepository tratamientoRepository;

    public DiagnosticosServiceImpl(DiagnosticosRepository diagnosticosRepository,
                                  TiposDiagnosticoRepository tiposDiagnosticoRepository,
                                  DiagnosticoConsultaRepository diagnosticoConsultaRepository,
                                  TratamientoRepository tratamientoRepository) {
        this.diagnosticosRepository = diagnosticosRepository;
        this.tiposDiagnosticoRepository = tiposDiagnosticoRepository;
        this.diagnosticoConsultaRepository = diagnosticoConsultaRepository;
        this.tratamientoRepository = tratamientoRepository;
    }

    private DiagnosticosDTO mapToDTO(Diagnosticos d) {
        return new DiagnosticosDTO(
                d.getIdDiagnostico(),
                d.getObservaciones(),
                d.getIdTipoDiagnostico(),
                d.getDescripcion(),
                d.getFecha(),
                d.getActivo()
        );
    }

    private Diagnosticos mapToEntity(DiagnosticosDTO dto) {
        Diagnosticos d = new Diagnosticos();
        d.setObservaciones(dto.observaciones() != null ? dto.observaciones().trim() : null);
        d.setIdTipoDiagnostico(dto.idTipoDiagnostico());
        d.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        d.setFecha(dto.fecha());
        d.setActivo(dto.activo());
        return d;
    }

    /**
     * Valida las reglas de negocio de diagnósticos:
     * 1. La descripción u observaciones son obligatorias.
     * 2. El tipo de diagnóstico (Presuntivo, Definitivo, etc.) debe existir y estar activo.
     * 3. La fecha del diagnóstico no puede ser futura respecto al día actual.
     */
    private void validarReglasDeNegocio(DiagnosticosDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del diagnóstico no pueden ser nulos");
        }

        if ((dto.descripcion() == null || dto.descripcion().trim().isEmpty())
                && (dto.observaciones() == null || dto.observaciones().trim().isEmpty())) {
            throw new IllegalArgumentException("La descripción o las observaciones del diagnóstico son obligatorias");
        }

        if (dto.idTipoDiagnostico() == null) {
            throw new IllegalArgumentException("El tipo de diagnóstico es obligatorio");
        }

        // RN-01: El tipo de diagnóstico debe existir y estar activo
        TiposDiagnostico tipo = tiposDiagnosticoRepository.findById(dto.idTipoDiagnostico())
                .orElseThrow(() -> new IllegalArgumentException("El tipo de diagnóstico con ID " + dto.idTipoDiagnostico() + " no existe"));

        if (tipo.getActivo() != null && !tipo.getActivo()) {
            throw new IllegalArgumentException("El tipo de diagnóstico '" + tipo.getDescripcion() + "' se encuentra inactivo");
        }

        // RN-02: La fecha del diagnóstico no puede ser futura
        if (dto.fecha() != null && dto.fecha().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha del diagnóstico no puede ser posterior a la fecha actual (" + LocalDate.now() + ")");
        }
    }

    @Override
    public List<DiagnosticosDTO> findAll() {
        return diagnosticosRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DiagnosticosDTO> findActivos() {
        return diagnosticosRepository.findByActivoTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DiagnosticosDTO> findByTipo(Long idTipoDiagnostico) {
        if (idTipoDiagnostico == null) {
            throw new IllegalArgumentException("El ID del tipo de diagnóstico es obligatorio");
        }
        return diagnosticosRepository.findByIdTipoDiagnostico(idTipoDiagnostico)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DiagnosticosDTO> findByFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        return diagnosticosRepository.findByFecha(fecha)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DiagnosticosDTO findById(Long id) {
        Diagnosticos entity = diagnosticosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el diagnóstico con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public DiagnosticosDTO crear(DiagnosticosDTO dto) {
        validarReglasDeNegocio(dto);

        Diagnosticos entity = mapToEntity(dto);
        if (entity.getFecha() == null) {
            entity.setFecha(LocalDate.now()); // Fecha de registro por defecto
        }
        if (entity.getActivo() == null) {
            entity.setActivo(true);
        }

        Diagnosticos guardado = diagnosticosRepository.save(entity);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public DiagnosticosDTO actualizar(DiagnosticosDTO dto, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del diagnóstico es obligatorio");
        }

        Diagnosticos existente = diagnosticosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el diagnóstico con ID: " + id));

        validarReglasDeNegocio(dto);

        existente.setObservaciones(dto.observaciones() != null ? dto.observaciones().trim() : null);
        existente.setIdTipoDiagnostico(dto.idTipoDiagnostico());
        existente.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        if (dto.fecha() != null) {
            existente.setFecha(dto.fecha());
        }
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        Diagnosticos actualizado = diagnosticosRepository.save(existente);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del diagnóstico es obligatorio");
        }

        Diagnosticos existente = diagnosticosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el diagnóstico con ID: " + id));

        // Integridad referencial: validar si está en consultas o tratamientos
        if (!diagnosticoConsultaRepository.findByIdDiagnostico(id).isEmpty()) {
            throw new IllegalStateException("No se puede eliminar el diagnóstico porque se encuentra vinculado a una consulta médica");
        }

        if (tratamientoRepository.existsByIdDiagnostico(id)) {
            throw new IllegalStateException("No se puede eliminar el diagnóstico porque tiene tratamientos médicos registrados");
        }

        // Borrado lógico
        existente.setActivo(false);
        diagnosticosRepository.save(existente);
    }
}
