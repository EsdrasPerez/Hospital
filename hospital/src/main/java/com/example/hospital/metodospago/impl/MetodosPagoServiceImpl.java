package com.example.hospital.metodospago.impl;

import com.example.hospital.pagos.PagosRepository;
import com.example.hospital.facturas.FacturasRepository;
import com.example.hospital.metodospago.MetodosPago;
import com.example.hospital.metodospago.MetodosPagoRepository;
import com.example.hospital.metodospago.MetodosPagoService;
import com.example.hospital.metodospago.dto.MetodosPagoDTO;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetodosPagoServiceImpl implements MetodosPagoService {

    private final MetodosPagoRepository metodosPagoRepository;
    private final FacturasRepository facturasRepository;
    private final PagosRepository pagosRepository;

    public MetodosPagoServiceImpl(MetodosPagoRepository metodosPagoRepository,
                                  FacturasRepository facturasRepository,
                                  PagosRepository pagosRepository) {
        this.metodosPagoRepository = metodosPagoRepository;
        this.facturasRepository = facturasRepository;
        this.pagosRepository = pagosRepository;
    }

    private MetodosPagoDTO mapToDTO(MetodosPago e) {
        return new MetodosPagoDTO(
                e.getIdMetodoPago(),
                e.getDescripcion(),
                e.getActivo()
        );
    }

    private void validar(MetodosPagoDTO dto, Long idActual) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos de el método de pago son requeridos");
        }
        if (dto.descripcion() == null || dto.descripcion().isBlank()) {
            throw new ReglaNegocioException("La descripción del método de pago es obligatoria");
        }
        if (dto.descripcion().trim().length() > 50) {
            throw new ReglaNegocioException("La descripción no puede exceder 50 caracteres");
        }
        boolean duplicado = idActual == null
                ? metodosPagoRepository.existsByDescripcionIgnoreCase(dto.descripcion().trim())
                : metodosPagoRepository.existsByDescripcionIgnoreCaseAndIdMetodoPagoNot(dto.descripcion().trim(), idActual);
        if (duplicado) {
            throw new DuplicadoException("Ya existe un método de pago con esa descripción");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetodosPagoDTO> findAll() {
        return metodosPagoRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MetodosPagoDTO findById(Long id) {
        return mapToDTO(buscar(id));
    }

    @Override
    @Transactional
    public MetodosPagoDTO crear(MetodosPagoDTO dto) {
        validar(dto, null);
        MetodosPago entity = new MetodosPago();
        entity.setDescripcion(dto.descripcion().trim());
        entity.setActivo(dto.activo() != null ? dto.activo() : true);
        return mapToDTO(metodosPagoRepository.save(entity));
    }

    @Override
    @Transactional
    public MetodosPagoDTO actualizar(MetodosPagoDTO dto, Long id) {
        MetodosPago existente = buscar(id);
        validar(dto, id);
        MetodosPago entity = existente;
        entity.setDescripcion(dto.descripcion().trim());
        if (dto.activo() != null) {
            entity.setActivo(dto.activo());
        }
        return mapToDTO(metodosPagoRepository.save(entity));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        MetodosPago existente = buscar(id);
        // Conflicto con dependencias (como documenta el Swagger): facturas o pagos que usan el método
        if (facturasRepository.existsByIdMetodoPago(id) || pagosRepository.existsByIdMetodoPago(id)) {
            throw new DuplicadoException("No se puede eliminar el método de pago porque lo usan facturas o pagos");
        }
        // Eliminación lógica para conservar el historial
        existente.setActivo(false);
        metodosPagoRepository.save(existente);
    }

    private MetodosPago buscar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID de el método de pago es obligatorio");
        }
        return metodosPagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el método de pago con ID: " + id));
    }
}
