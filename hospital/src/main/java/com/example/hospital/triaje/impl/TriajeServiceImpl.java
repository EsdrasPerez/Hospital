package com.example.hospital.triaje.impl;

import com.example.hospital.emergencia.EmergenciaRepository;
import com.example.hospital.triaje.Triaje;
import com.example.hospital.triaje.TriajeRepository;
import com.example.hospital.triaje.TriajeService;
import com.example.hospital.triaje.dto.TriajeDTO;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TriajeServiceImpl implements TriajeService {

    private final TriajeRepository triajeRepository;
    private final EmergenciaRepository emergenciaRepository;

    public TriajeServiceImpl(TriajeRepository triajeRepository, EmergenciaRepository emergenciaRepository) {
        this.triajeRepository = triajeRepository;
        this.emergenciaRepository = emergenciaRepository;
    }

    private TriajeDTO mapToDTO(Triaje e) {
        return new TriajeDTO(
                e.getIdTriaje(),
                e.getNivelDeEmergencia(),
                e.getDescripcion(),
                e.getActivo()
        );
    }

    private void validar(TriajeDTO dto, Long idActual) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos de el triaje son requeridos");
        }
        // El nivel obligatorio y los tamaños los valida Bean Validation en TriajeDTO (400 con detalles)
        String nivel = dto.nivelDeEmergencia().trim();
        boolean duplicado = idActual == null
                ? triajeRepository.existsByNivelDeEmergenciaIgnoreCase(nivel)
                : triajeRepository.existsByNivelDeEmergenciaIgnoreCaseAndIdTriajeNot(nivel, idActual);
        if (duplicado) {
            throw new DuplicadoException("Ya existe un triaje con el nivel " + nivel);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TriajeDTO> findAll() {
        return triajeRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TriajeDTO findById(Long id) {
        return mapToDTO(buscar(id));
    }

    @Override
    @Transactional
    public TriajeDTO crear(TriajeDTO dto) {
        validar(dto, null);
        Triaje entity = new Triaje();
        entity.setNivelDeEmergencia(dto.nivelDeEmergencia().trim());
        entity.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        entity.setActivo(dto.activo() != null ? dto.activo() : true);
        return mapToDTO(triajeRepository.save(entity));
    }

    @Override
    @Transactional
    public TriajeDTO actualizar(TriajeDTO dto, Long id) {
        Triaje existente = buscar(id);
        validar(dto, id);
        Triaje entity = existente;
        entity.setNivelDeEmergencia(dto.nivelDeEmergencia().trim());
        entity.setDescripcion(dto.descripcion());
        if (dto.activo() != null) {
            entity.setActivo(dto.activo());
        }
        return mapToDTO(triajeRepository.save(entity));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Triaje existente = buscar(id);
        // Conflicto con dependientes: no se desactiva un nivel que usan emergencias activas (409)
        if (emergenciaRepository.existsByIdTriajeAndActivoTrue(id)) {
            throw new IllegalStateException("No se puede desactivar el nivel de triaje porque hay ingresos a emergencia activos que lo usan.");
        }
        // Eliminación lógica para conservar el historial
        existente.setActivo(false);
        triajeRepository.save(existente);
    }

    private Triaje buscar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID de el triaje es obligatorio");
        }
        return triajeRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el triaje con ID: " + id));
    }
}
