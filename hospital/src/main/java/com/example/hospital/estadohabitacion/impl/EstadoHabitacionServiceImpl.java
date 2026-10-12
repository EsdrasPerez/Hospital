package com.example.hospital.estadohabitacion.impl;

import com.example.hospital.estadohabitacion.EstadoHabitacion;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.estadohabitacion.EstadoHabitacionRepository;
import com.example.hospital.estadohabitacion.EstadoHabitacionService;
import com.example.hospital.estadohabitacion.dto.EstadoHabitacionDTO;
import com.example.hospital.habitacion.HabitacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EstadoHabitacionServiceImpl implements EstadoHabitacionService {

    // Valores base fijos requeridos por la lógica de gestión de camas y hospitalizaciones
    public static final String DISPONIBLE = "Disponible";
    public static final String OCUPADA = "Ocupada";
    public static final String MANTENIMIENTO = "Mantenimiento";

    private static final Set<String> ESTADOS_BASE = Set.of(
            DISPONIBLE, OCUPADA, MANTENIMIENTO
    );

    private final EstadoHabitacionRepository estadoHabitacionRepository;
    private final HabitacionRepository habitacionRepository;

    public EstadoHabitacionServiceImpl(EstadoHabitacionRepository estadoHabitacionRepository,
                                       HabitacionRepository habitacionRepository) {
        this.estadoHabitacionRepository = estadoHabitacionRepository;
        this.habitacionRepository = habitacionRepository;
    }

    private EstadoHabitacionDTO mapToDTO(EstadoHabitacion e) {
        return new EstadoHabitacionDTO(
                e.getIdEstadoHabitacion(),
                e.getNombre(),
                e.getDescripcion(),
                e.getActivo()
        );
    }

    private EstadoHabitacion mapToEntity(EstadoHabitacionDTO dto) {
        EstadoHabitacion e = new EstadoHabitacion();
        e.setNombre(dto.nombre());
        e.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        e.setActivo(dto.activo());
        return e;
    }

    private String obtenerNombreCanonica(String descripcion) {
        if (descripcion == null) return "";
        for (String base : ESTADOS_BASE) {
            if (base.equalsIgnoreCase(descripcion.trim())) {
                return base;
            }
        }
        return descripcion.trim();
    }

    @Override
    public List<EstadoHabitacionDTO> findAll() {
        return estadoHabitacionRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EstadoHabitacionDTO findById(Long id) {
        EstadoHabitacion entity = estadoHabitacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estado de habitación con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public EstadoHabitacionDTO crear(EstadoHabitacionDTO dto) {
        if (dto == null || dto.descripcion() == null || dto.descripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del estado de habitación es obligatoria");
        }

        String canonico = obtenerNombreCanonica(dto.descripcion());

        if (estadoHabitacionRepository.existsByDescripcionIgnoreCase(canonico)) {
            throw new IllegalArgumentException("Ya existe un estado de habitación con la descripción: " + canonico);
        }

        EstadoHabitacion entity = mapToEntity(dto);
        entity.setDescripcion(canonico);
        if (entity.getNombre() == null || entity.getNombre().isBlank()) {
            entity.setNombre(canonico);
        }
        if (entity.getActivo() == null) {
            entity.setActivo(true);
        }

        EstadoHabitacion guardado = estadoHabitacionRepository.save(entity);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public EstadoHabitacionDTO actualizar(EstadoHabitacionDTO dto, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del estado de habitación es obligatorio");
        }

        EstadoHabitacion existente = estadoHabitacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estado de habitación con ID: " + id));

        // Regla: No se permite renombrar un estado base fijo usado por el código del hospital
        if (ESTADOS_BASE.contains(existente.getDescripcion())) {
            if (dto.descripcion() != null && !dto.descripcion().equalsIgnoreCase(existente.getDescripcion())) {
                throw new IllegalArgumentException("No se permite renombrar el estado base fijo '" +
                        existente.getDescripcion() + "'");
            }
        }

        if (dto.descripcion() != null && !dto.descripcion().trim().isEmpty()) {
            String canonico = obtenerNombreCanonica(dto.descripcion());
            if (estadoHabitacionRepository.existsByDescripcionIgnoreCaseAndIdEstadoHabitacionNot(canonico, id)) {
                throw new IllegalArgumentException("Ya existe otro estado de habitación con la descripción: " + canonico);
            }
            existente.setDescripcion(canonico);
        }

        if (dto.nombre() != null) {
            existente.setNombre(dto.nombre());
        }
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        EstadoHabitacion actualizado = estadoHabitacionRepository.save(existente);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del estado de habitación es obligatorio");
        }

        EstadoHabitacion existente = estadoHabitacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estado de habitación con ID: " + id));

        // Regla: No se pueden borrar estados base fijos (Disponible, Ocupada, Mantenimiento)
        if (ESTADOS_BASE.contains(existente.getDescripcion())) {
            throw new IllegalArgumentException("No se puede eliminar el estado base '" +
                    existente.getDescripcion() + "' requerido por la lógica del sistema");
        }

        // Regla de Integridad: No eliminar si está asignado a habitaciones
        boolean asignadoAHabitaciones = habitacionRepository.existsByIdEstadoHabitacion(id);
        if (asignadoAHabitaciones) {
            throw new IllegalArgumentException("No se puede eliminar el estado porque se encuentra asignado a una o más habitaciones");
        }

        // Eliminación lógica
        existente.setActivo(false);
        estadoHabitacionRepository.save(existente);
    }
}
