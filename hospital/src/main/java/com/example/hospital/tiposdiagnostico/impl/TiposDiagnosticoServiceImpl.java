package com.example.hospital.tiposdiagnostico.impl;

import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.diagnosticos.DiagnosticosRepository;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.tiposdiagnostico.TiposDiagnostico;
import com.example.hospital.tiposdiagnostico.TiposDiagnosticoRepository;
import com.example.hospital.tiposdiagnostico.TiposDiagnosticoService;
import com.example.hospital.tiposdiagnostico.dto.TiposDiagnosticoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TiposDiagnosticoServiceImpl implements TiposDiagnosticoService {

    // Valores base clínicos inicializados en BD: Presuntivo, Definitivo, Diferencial
    public static final String PRESUNTIVO = "Presuntivo";
    public static final String DEFINITIVO = "Definitivo";
    public static final String DIFERENCIAL = "Diferencial";

    private static final Set<String> TIPOS_BASE = Set.of(
            PRESUNTIVO, DEFINITIVO, DIFERENCIAL
    );

    private final TiposDiagnosticoRepository tiposDiagnosticoRepository;
    private final DiagnosticosRepository diagnosticosRepository;

    public TiposDiagnosticoServiceImpl(TiposDiagnosticoRepository tiposDiagnosticoRepository,
                                       DiagnosticosRepository diagnosticosRepository) {
        this.tiposDiagnosticoRepository = tiposDiagnosticoRepository;
        this.diagnosticosRepository = diagnosticosRepository;
    }

    private TiposDiagnosticoDTO mapToDTO(TiposDiagnostico t) {
        return new TiposDiagnosticoDTO(
                t.getIdTipoDiagnostico(),
                t.getDescripcion(),
                t.getActivo()
        );
    }

    private TiposDiagnostico mapToEntity(TiposDiagnosticoDTO dto) {
        TiposDiagnostico t = new TiposDiagnostico();
        t.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        t.setActivo(dto.activo() != null ? dto.activo() : true); // activo por defecto al crear
        return t;
    }

    private String obtenerNombreCanonica(String descripcion) {
        if (descripcion == null) return "";
        for (String base : TIPOS_BASE) {
            if (base.equalsIgnoreCase(descripcion.trim())) {
                return base;
            }
        }
        return descripcion.trim();
    }

    @Override
    public List<TiposDiagnosticoDTO> findAll() {
        return tiposDiagnosticoRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TiposDiagnosticoDTO findById(Long id) {
        TiposDiagnostico entity = tiposDiagnosticoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tipo de diagnóstico con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public TiposDiagnosticoDTO crear(TiposDiagnosticoDTO dto) {
        if (dto == null || dto.descripcion() == null || dto.descripcion().trim().isEmpty()) {
            throw new ReglaNegocioException("La descripción del tipo de diagnóstico es obligatoria");
        }

        String canonico = obtenerNombreCanonica(dto.descripcion());

        if (tiposDiagnosticoRepository.existsByDescripcionIgnoreCase(canonico)) {
            throw new DuplicadoException("Ya existe un tipo de diagnóstico con la descripción: " + canonico);
        }

        TiposDiagnostico entity = mapToEntity(dto);
        entity.setDescripcion(canonico);
        if (entity.getActivo() == null) {
            entity.setActivo(true);
        }

        TiposDiagnostico guardado = tiposDiagnosticoRepository.save(entity);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public TiposDiagnosticoDTO actualizar(TiposDiagnosticoDTO dto, Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID del tipo de diagnóstico es obligatorio");
        }

        TiposDiagnostico existente = tiposDiagnosticoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tipo de diagnóstico con ID: " + id));

        // Regla: No renombrar los tipos base del sistema
        if (TIPOS_BASE.contains(existente.getDescripcion())) {
            if (dto.descripcion() != null && !dto.descripcion().equalsIgnoreCase(existente.getDescripcion())) {
                throw new ReglaNegocioException("No se permite renombrar el tipo de diagnóstico base '" +
                        existente.getDescripcion() + "'");
            }
        }

        if (dto.descripcion() != null && !dto.descripcion().trim().isEmpty()) {
            String canonico = obtenerNombreCanonica(dto.descripcion());
            if (tiposDiagnosticoRepository.existsByDescripcionIgnoreCaseAndIdTipoDiagnosticoNot(canonico, id)) {
                throw new DuplicadoException("Ya existe otro tipo de diagnóstico con la descripción: " + canonico);
            }
            existente.setDescripcion(canonico);
        }

        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        TiposDiagnostico actualizado = tiposDiagnosticoRepository.save(existente);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID del tipo de diagnóstico es obligatorio");
        }

        TiposDiagnostico existente = tiposDiagnosticoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tipo de diagnóstico con ID: " + id));

        // Regla: No eliminar tipos base
        if (TIPOS_BASE.contains(existente.getDescripcion())) {
            throw new ReglaNegocioException("No se puede eliminar el tipo de diagnóstico base '" +
                    existente.getDescripcion() + "'");
        }

        // Regla de integridad: No eliminar si está asignado a diagnósticos
        boolean asignado = diagnosticosRepository.existsByIdTipoDiagnostico(id);
        if (asignado) {
            throw new DuplicadoException("No se puede eliminar el tipo de diagnóstico porque está asociado a uno o más diagnósticos médicos");
        }

        // Eliminación lógica
        existente.setActivo(false);
        tiposDiagnosticoRepository.save(existente);
    }
}
