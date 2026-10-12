package com.example.hospital.rol.impl;

import com.example.hospital.rol.Rol;
import com.example.hospital.rol.RolRepository;
import com.example.hospital.rol.RolService;
import com.example.hospital.rol.dto.RolDTO;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    public RolServiceImpl(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    private RolDTO mapToDTO(Rol e) {
        return new RolDTO(
                e.getIdRol(),
                e.getNombre(),
                e.getDescripcion(),
                e.getActivo()
        );
    }

    private void validar(RolDTO dto, Long idActual) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos de el rol son requeridos");
        }
        if (dto.nombre() == null || dto.nombre().isBlank()) {
            throw new ReglaNegocioException("El nombre del rol es obligatorio");
        }
        String nombre = dto.nombre().trim();
        boolean duplicado = idActual == null
                ? rolRepository.existsByNombreIgnoreCase(nombre)
                : rolRepository.existsByNombreIgnoreCaseAndIdRolNot(nombre, idActual);
        if (duplicado) {
            throw new DuplicadoException("Ya existe un rol con el nombre " + nombre);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolDTO> findAll() {
        return rolRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RolDTO findById(Long id) {
        return mapToDTO(buscar(id));
    }

    @Override
    @Transactional
    public RolDTO crear(RolDTO dto) {
        validar(dto, null);
        Rol entity = new Rol();
        entity.setNombre(dto.nombre().trim());
        entity.setDescripcion(dto.descripcion());
        entity.setActivo(dto.activo() != null ? dto.activo() : true);
        return mapToDTO(rolRepository.save(entity));
    }

    @Override
    @Transactional
    public RolDTO actualizar(RolDTO dto, Long id) {
        Rol existente = buscar(id);
        validar(dto, id);
        Rol entity = existente;
        entity.setNombre(dto.nombre().trim());
        entity.setDescripcion(dto.descripcion());
        if (dto.activo() != null) {
            entity.setActivo(dto.activo());
        }
        return mapToDTO(rolRepository.save(entity));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Rol existente = buscar(id);
        // Eliminación lógica para conservar el historial
        existente.setActivo(false);
        rolRepository.save(existente);
    }

    private Rol buscar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID de el rol es obligatorio");
        }
        return rolRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el rol con ID: " + id));
    }
}
