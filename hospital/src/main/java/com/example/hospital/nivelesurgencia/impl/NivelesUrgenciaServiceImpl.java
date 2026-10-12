package com.example.hospital.nivelesurgencia.impl;

import com.example.hospital.nivelesurgencia.NivelesUrgencia;
import com.example.hospital.nivelesurgencia.NivelesUrgenciaRepository;
import com.example.hospital.nivelesurgencia.NivelesUrgenciaService;
import com.example.hospital.nivelesurgencia.dto.NivelesUrgenciaDTO;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NivelesUrgenciaServiceImpl implements NivelesUrgenciaService {

    private final NivelesUrgenciaRepository nivelesUrgenciaRepository;

    public NivelesUrgenciaServiceImpl(NivelesUrgenciaRepository nivelesUrgenciaRepository) {
        this.nivelesUrgenciaRepository = nivelesUrgenciaRepository;
    }

    private NivelesUrgenciaDTO mapToDTO(NivelesUrgencia e) {
        return new NivelesUrgenciaDTO(
                e.getIdNivelUrgencia(),
                e.getCodigo(),
                e.getDescripcion(),
                e.getTiempoMaxEsperaMin(),
                e.getColor(),
                e.getActivo()
        );
    }

    private void validar(NivelesUrgenciaDTO dto, Long idActual) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos de el nivel de urgencia son requeridos");
        }
        if (dto.codigo() == null || !dto.codigo().trim().matches("(?i)N\\d")) {
            throw new ReglaNegocioException("El código debe tener el formato N1, N2, N3...");
        }
        if (dto.descripcion() == null || dto.descripcion().isBlank()) {
            throw new ReglaNegocioException("La descripción del nivel de urgencia es obligatoria");
        }
        if (dto.tiempoMaxEsperaMin() == null || dto.tiempoMaxEsperaMin() < 0) {
            throw new ReglaNegocioException("El tiempo máximo de espera debe ser mayor o igual a 0");
        }
        String codigo = dto.codigo().trim();
        boolean duplicado = idActual == null
                ? nivelesUrgenciaRepository.existsByCodigoIgnoreCase(codigo)
                : nivelesUrgenciaRepository.existsByCodigoIgnoreCaseAndIdNivelUrgenciaNot(codigo, idActual);
        if (duplicado) {
            throw new DuplicadoException("Ya existe un nivel de urgencia con el código " + codigo);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NivelesUrgenciaDTO> findAll() {
        return nivelesUrgenciaRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NivelesUrgenciaDTO findById(Long id) {
        return mapToDTO(buscar(id));
    }

    @Override
    @Transactional
    public NivelesUrgenciaDTO crear(NivelesUrgenciaDTO dto) {
        validar(dto, null);
        NivelesUrgencia entity = new NivelesUrgencia();
        entity.setCodigo(dto.codigo().trim().toUpperCase());
        entity.setDescripcion(dto.descripcion().trim());
        entity.setTiempoMaxEsperaMin(dto.tiempoMaxEsperaMin());
        entity.setColor(dto.color());
        entity.setActivo(dto.activo() != null ? dto.activo() : true);
        return mapToDTO(nivelesUrgenciaRepository.save(entity));
    }

    @Override
    @Transactional
    public NivelesUrgenciaDTO actualizar(NivelesUrgenciaDTO dto, Long id) {
        NivelesUrgencia existente = buscar(id);
        validar(dto, id);
        NivelesUrgencia entity = existente;
        entity.setCodigo(dto.codigo().trim().toUpperCase());
        entity.setDescripcion(dto.descripcion().trim());
        entity.setTiempoMaxEsperaMin(dto.tiempoMaxEsperaMin());
        entity.setColor(dto.color());
        if (dto.activo() != null) {
            entity.setActivo(dto.activo());
        }
        return mapToDTO(nivelesUrgenciaRepository.save(entity));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        NivelesUrgencia existente = buscar(id);
        // Eliminación lógica para conservar el historial
        existente.setActivo(false);
        nivelesUrgenciaRepository.save(existente);
    }

    private NivelesUrgencia buscar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID de el nivel de urgencia es obligatorio");
        }
        return nivelesUrgenciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el nivel de urgencia con ID: " + id));
    }
}
