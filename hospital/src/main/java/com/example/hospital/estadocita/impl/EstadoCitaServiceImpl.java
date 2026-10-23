package com.example.hospital.estadocita.impl;

import com.example.hospital.estadocita.EstadoCita;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.estadocita.EstadoCitaRepository;
import com.example.hospital.estadocita.EstadoCitaService;
import com.example.hospital.estadocita.dto.EstadoCitaDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EstadoCitaServiceImpl implements EstadoCitaService {

    // RN-EC-01: Valores base válidos para descripcion
    public static final String PROGRAMADA = "Programada";
    public static final String CONFIRMADA = "Confirmada";
    public static final String EN_CURSO = "En curso";
    public static final String ATENDIDA = "Atendida";
    public static final String CANCELADA = "Cancelada";
    public static final String NO_ASISTIO = "No asistió";
    public static final String REPROGRAMADA = "Reprogramada";

    private static final Set<String> ESTADOS_BASE = Set.of(
            PROGRAMADA, CONFIRMADA, EN_CURSO, ATENDIDA, CANCELADA, NO_ASISTIO, REPROGRAMADA
    );

    // Estados finales que no pueden cambiar de estado
    private static final Set<String> ESTADOS_FINALES = Set.of(
            ATENDIDA, CANCELADA, NO_ASISTIO
    );

    // RN-EC-02: Mapa de transiciones permitidas
    // Programada → Confirmada, Cancelada, No asistió, Reprogramada
    // Confirmada → En curso, Cancelada, No asistió, Reprogramada
    // En curso   → Atendida
    // Atendida, Cancelada, No asistió → Finales (no permiten transición)
    private static final Map<String, Set<String>> TRANSICIONES_PERMITIDAS = Map.of(
            PROGRAMADA, Set.of(CONFIRMADA, CANCELADA, NO_ASISTIO, REPROGRAMADA),
            CONFIRMADA, Set.of(EN_CURSO, CANCELADA, NO_ASISTIO, REPROGRAMADA),
            EN_CURSO, Set.of(ATENDIDA),
            REPROGRAMADA, Set.of(CONFIRMADA, CANCELADA, NO_ASISTIO)
    );

    private final EstadoCitaRepository estadoCitaRepository;

    public EstadoCitaServiceImpl(EstadoCitaRepository estadoCitaRepository) {
        this.estadoCitaRepository = estadoCitaRepository;
    }

    private EstadoCitaDTO mapToDTO(EstadoCita e) {
        return new EstadoCitaDTO(
                e.getIdEstado(),
                e.getNombre(),
                e.getDescripcion(),
                e.getActivo()
        );
    }

    private EstadoCita mapToEntity(EstadoCitaDTO dto) {
        EstadoCita e = new EstadoCita();
        e.setNombre(dto.nombre());
        e.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        e.setActivo(dto.activo());
        return e;
    }

    private String normalizar(String descripcion) {
        if (descripcion == null) return "";
        return descripcion.trim().toLowerCase();
    }

    private String obtenerNombreCanonica(String descripcion) {
        String normal = normalizar(descripcion);
        for (String base : ESTADOS_BASE) {
            if (base.toLowerCase().equals(normal)) {
                return base;
            }
        }
        return descripcion;
    }

    @Override
    public List<EstadoCitaDTO> findAll() {
        return estadoCitaRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EstadoCitaDTO findById(Long id) {
        EstadoCita entity = estadoCitaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estado de cita con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public EstadoCitaDTO crear(EstadoCitaDTO dto) {
        if (dto == null || dto.descripcion() == null || dto.descripcion().trim().isEmpty()) {
            throw new ReglaNegocioException("La descripción del estado de cita es obligatoria");
        }

        String canonico = obtenerNombreCanonica(dto.descripcion().trim());

        // RN-EC-01: Solo admite los valores base
        if (!ESTADOS_BASE.contains(canonico)) {
            throw new ReglaNegocioException("Descripción no permitida. Valores base válidos: " + ESTADOS_BASE);
        }

        // RN-EC-01: descripcion única
        if (estadoCitaRepository.existsByDescripcionIgnoreCase(canonico)) {
            throw new DuplicadoException("Ya existe un estado de cita con la descripción: " + canonico);
        }

        EstadoCita entity = mapToEntity(dto);
        entity.setDescripcion(canonico);
        if (entity.getNombre() == null || entity.getNombre().isBlank()) {
            entity.setNombre(canonico);
        }
        if (entity.getActivo() == null) {
            entity.setActivo(true);
        }

        EstadoCita guardado = estadoCitaRepository.save(entity);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public EstadoCitaDTO actualizar(EstadoCitaDTO dto, Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID del estado es obligatorio");
        }

        EstadoCita existente = estadoCitaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estado de cita con ID: " + id));

        // RN-EC-03: Los estados del flujo son valores fijos usados por el código; no se pueden renombrar
        if (ESTADOS_BASE.contains(existente.getDescripcion())) {
            if (dto.descripcion() != null && !dto.descripcion().equalsIgnoreCase(existente.getDescripcion())) {
                throw new ReglaNegocioException("RN-EC-03: No se permite renombrar un estado base fijo del flujo (" +
                        existente.getDescripcion() + ")");
            }
        }

        if (dto.descripcion() != null && !dto.descripcion().trim().isEmpty()) {
            String canonico = obtenerNombreCanonica(dto.descripcion().trim());
            if (!ESTADOS_BASE.contains(canonico)) {
                throw new ReglaNegocioException("Descripción no permitida. Valores base válidos: " + ESTADOS_BASE);
            }
            if (estadoCitaRepository.existsByDescripcionIgnoreCaseAndIdEstadoNot(canonico, id)) {
                throw new DuplicadoException("Ya existe otro estado con la descripción: " + canonico);
            }
            existente.setDescripcion(canonico);
        }

        if (dto.nombre() != null) {
            existente.setNombre(dto.nombre());
        }
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        EstadoCita actualizado = estadoCitaRepository.save(existente);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID del estado es obligatorio");
        }

        EstadoCita existente = estadoCitaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estado de cita con ID: " + id));

        // RN-EC-03: Los estados del flujo son valores fijos usados por el código; no se pueden borrar
        if (ESTADOS_BASE.contains(existente.getDescripcion())) {
            throw new ReglaNegocioException("RN-EC-03: No se puede eliminar el estado base '" +
                    existente.getDescripcion() + "' porque es requerido por el flujo del sistema");
        }

        // Eliminación lógica
        existente.setActivo(false);
        estadoCitaRepository.save(existente);
    }

    @Override
    public boolean esTransicionPermitida(String estadoOrigen, String estadoDestino) {
        if (estadoOrigen == null || estadoDestino == null) {
            return false;
        }

        String origenCanonica = obtenerNombreCanonica(estadoOrigen);
        String destinoCanonica = obtenerNombreCanonica(estadoDestino);

        // Si ya está en un estado final, no puede cambiar
        if (ESTADOS_FINALES.contains(origenCanonica)) {
            return false;
        }

        Set<String> destinosValidos = TRANSICIONES_PERMITIDAS.get(origenCanonica);
        return destinosValidos != null && destinosValidos.contains(destinoCanonica);
    }

    @Override
    public void validarTransicion(String estadoOrigen, String estadoDestino) {
        if (estadoOrigen == null || estadoDestino == null) {
            throw new ReglaNegocioException("El estado origen y destino son requeridos");
        }

        String origenCanonica = obtenerNombreCanonica(estadoOrigen);
        String destinoCanonica = obtenerNombreCanonica(estadoDestino);

        if (ESTADOS_FINALES.contains(origenCanonica)) {
            throw new ReglaNegocioException("RN-EC-02: Una cita en estado '" + origenCanonica +
                    "' es final y no puede cambiar de estado");
        }

        if (!esTransicionPermitida(origenCanonica, destinoCanonica)) {
            throw new ReglaNegocioException("RN-EC-02: Transición de estado no permitida: '" +
                    origenCanonica + "' → '" + destinoCanonica + "'");
        }
    }
}
