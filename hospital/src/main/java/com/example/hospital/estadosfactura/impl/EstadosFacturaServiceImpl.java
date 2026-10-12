package com.example.hospital.estadosfactura.impl;

import com.example.hospital.estadosfactura.EstadosFactura;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.estadosfactura.EstadosFacturaRepository;
import com.example.hospital.estadosfactura.EstadosFacturaService;
import com.example.hospital.estadosfactura.dto.EstadosFacturaDTO;
import com.example.hospital.facturas.FacturasRepository;
import com.example.hospital.shared.exception.ReglaNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EstadosFacturaServiceImpl implements EstadosFacturaService {

    // Estados base esenciales del sistema fiscal/hospitalario
    public static final String PENDIENTE = "Pendiente";
    public static final String PAGADA = "Pagada";
    public static final String CANCELADA = "Cancelada";

    private static final Set<String> ESTADOS_BASE = Set.of(
            PENDIENTE.toUpperCase(),
            PAGADA.toUpperCase(),
            CANCELADA.toUpperCase()
    );

    private final EstadosFacturaRepository repository;
    private final FacturasRepository facturasRepository;

    public EstadosFacturaServiceImpl(EstadosFacturaRepository repository,
                                     FacturasRepository facturasRepository) {
        this.repository = repository;
        this.facturasRepository = facturasRepository;
    }

    private EstadosFacturaDTO mapToDTO(EstadosFactura e) {
        return new EstadosFacturaDTO(
                e.getIdEstadoFactura(),
                e.getDescripcion(),
                e.getActivo()
        );
    }

    private EstadosFactura mapToEntity(EstadosFacturaDTO dto) {
        EstadosFactura e = new EstadosFactura();
        e.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        e.setActivo(dto.activo() != null ? dto.activo() : true);
        return e;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadosFacturaDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadosFacturaDTO> findActivos() {
        return repository.findByActivoTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EstadosFacturaDTO findById(Long id) {
        EstadosFactura entity = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estado de factura con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadosFacturaDTO findByDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new ReglaNegocioException("La descripción de búsqueda es obligatoria.");
        }
        EstadosFactura entity = repository.findByDescripcionIgnoreCase(descripcion.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estado de factura con descripción: " + descripcion));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public EstadosFacturaDTO crear(EstadosFacturaDTO dto) {
        validarReglasDeNegocio(dto, null);

        EstadosFactura entity = mapToEntity(dto);
        EstadosFactura guardado = repository.save(entity);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public EstadosFacturaDTO actualizar(EstadosFacturaDTO dto, Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID es obligatorio para la actualización.");
        }

        EstadosFactura existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estado de factura con ID: " + id));

        validarReglasDeNegocio(dto, id);

        // RN-EF-03: Protección de estados base esenciales del sistema
        boolean eraBase = esEstadoBase(existente.getDescripcion());
        if (eraBase && !existente.getDescripcion().equalsIgnoreCase(dto.descripcion().trim())) {
            throw new ReglaNegocioException("No se permite modificar la descripción de los estados base del sistema contable: Pendiente, Pagada, Cancelada.");
        }

        if (eraBase && Boolean.FALSE.equals(dto.activo())) {
            throw new ReglaNegocioException("No se permite desactivar un estado base esencial del sistema contable.");
        }

        existente.setDescripcion(dto.descripcion().trim());
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        EstadosFactura actualizado = repository.save(existente);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID no puede ser nulo.");
        }

        EstadosFactura existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estado de factura con ID: " + id));

        // RN-EF-04: Protección contra eliminación de estados base
        if (esEstadoBase(existente.getDescripcion())) {
            throw new ReglaNegocioException("No está permitido eliminar los estados base esenciales del sistema (" + existente.getDescripcion() + ").");
        }

        // RN-EF-05: Integridad referencial contra facturas
        if (facturasRepository.existsByIdEstadoFactura(id)) {
            throw new ReglaNegocioException("No se puede eliminar el estado de factura porque existen facturas registradas con este estado.");
        }

        // RN-EF-06: Borrado lógico sistemático
        existente.setActivo(false);
        repository.save(existente);
    }

    private void validarReglasDeNegocio(EstadosFacturaDTO dto, Long idActualizacion) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos del estado de factura son requeridos.");
        }

        // RN-EF-01: Descripción obligatoria y longitud
        if (dto.descripcion() == null || dto.descripcion().trim().isEmpty()) {
            throw new ReglaNegocioException("La descripción del estado de factura es obligatoria.");
        }

        String desc = dto.descripcion().trim();
        if (desc.length() > 30) {
            throw new ReglaNegocioException("La descripción no puede exceder los 30 caracteres.");
        }

        // RN-EF-02: Unicidad insensible a mayúsculas/minúsculas
        if (idActualizacion == null) {
            if (repository.existsByDescripcionIgnoreCase(desc)) {
                throw new ReglaNegocioException("Ya existe un estado de factura con la descripción: " + desc);
            }
        } else {
            if (repository.existsByDescripcionIgnoreCaseAndIdEstadoFacturaNot(desc, idActualizacion)) {
                throw new ReglaNegocioException("Ya existe otro estado de factura con la descripción: " + desc);
            }
        }
    }

    private boolean esEstadoBase(String descripcion) {
        if (descripcion == null) return false;
        return ESTADOS_BASE.contains(descripcion.trim().toUpperCase());
    }
}
