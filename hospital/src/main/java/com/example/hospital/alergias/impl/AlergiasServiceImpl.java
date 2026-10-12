package com.example.hospital.alergias.impl;

import com.example.hospital.alergias.Alergias;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.alergias.AlergiasRepository;
import com.example.hospital.alergias.AlergiasService;
import com.example.hospital.alergias.dto.AlergiasDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlergiasServiceImpl implements AlergiasService {

    private final AlergiasRepository alergiasRepository;

    public AlergiasServiceImpl(AlergiasRepository alergiasRepository) {
        this.alergiasRepository = alergiasRepository;
    }

    private AlergiasDTO mapToDTO(Alergias entity) {
        return new AlergiasDTO(
                entity.getIdAlergias(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getActivo()
        );
    }

    private Alergias mapToEntity(AlergiasDTO dto) {
        Alergias entity = new Alergias();
        entity.setNombre(dto.nombre());
        entity.setDescripcion(dto.descripcion());
        entity.setActivo(dto.activo());
        return entity;
    }

    @Override
    public List<AlergiasDTO> findAll() {
        return alergiasRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AlergiasDTO findById(Long id) {
        Alergias entity = alergiasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la alergia con id: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public AlergiasDTO crear(AlergiasDTO dto) {
        if (dto == null || dto.nombre() == null) {
            throw new IllegalArgumentException("El nombre de la alergia es obligatorio");
        }
        Alergias entity = mapToEntity(dto);
        Alergias guardado = alergiasRepository.save(entity);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public AlergiasDTO actualizar(AlergiasDTO dto, Long id) {
        if (dto == null || dto.nombre() == null || id == null) {
            throw new IllegalArgumentException("Datos para actualizar inválidos");
        }
        Alergias existente = alergiasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la alergia a actualizar con id: " + id));

        existente.setNombre(dto.nombre());
        existente.setDescripcion(dto.descripcion());
        existente.setActivo(dto.activo());

        Alergias actualizado = alergiasRepository.save(existente);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Alergias existente = alergiasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la alergia a eliminar con id: " + id));
        existente.setActivo(false);
        alergiasRepository.save(existente);
    }
}
