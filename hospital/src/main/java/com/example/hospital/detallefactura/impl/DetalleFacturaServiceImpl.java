package com.example.hospital.detallefactura.impl;

import com.example.hospital.detallefactura.DetalleFactura;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.detallefactura.DetalleFacturaRepository;
import com.example.hospital.detallefactura.DetalleFacturaService;
import com.example.hospital.detallefactura.dto.DetalleFacturaDTO;
import com.example.hospital.facturas.Facturas;
import com.example.hospital.facturas.FacturasRepository;
import com.example.hospital.shared.exception.ReglaNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DetalleFacturaServiceImpl implements DetalleFacturaService {

    public static final Long ESTADO_FACTURA_PAGADA = 2L;
    public static final Long ESTADO_FACTURA_CANCELADA = 3L;

    private final DetalleFacturaRepository detalleFacturaRepository;
    private final FacturasRepository facturasRepository;

    public DetalleFacturaServiceImpl(DetalleFacturaRepository detalleFacturaRepository,
                                     FacturasRepository facturasRepository) {
        this.detalleFacturaRepository = detalleFacturaRepository;
        this.facturasRepository = facturasRepository;
    }

    private DetalleFacturaDTO mapToDTO(DetalleFactura d) {
        return new DetalleFacturaDTO(
                d.getIdDetalle(),
                d.getIdFactura(),
                d.getConcepto(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getImporte()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetalleFacturaDTO> findAll() {
        return detalleFacturaRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DetalleFacturaDTO findById(Long id) {
        DetalleFactura entity = detalleFacturaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el detalle de factura con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetalleFacturaDTO> findByFactura(Long idFactura) {
        if (idFactura == null) {
            throw new ReglaNegocioException("El ID de la factura es requerido");
        }
        return detalleFacturaRepository.findByIdFactura(idFactura)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DetalleFacturaDTO crear(DetalleFacturaDTO dto) {
        validarReglasDeNegocio(dto);

        // RN-140: La factura debe existir
        Facturas factura = facturasRepository.findById(dto.idFactura())
                .orElseThrow(() -> new ReglaNegocioException("RN-140: La factura con ID " + dto.idFactura() + " no existe."));

        if (!Boolean.TRUE.equals(factura.getActivo())) {
            throw new ReglaNegocioException("La factura seleccionada se encuentra inactiva.");
        }

        // RN-146: No se pueden agregar detalles a facturas pagadas o canceladas
        validarFacturaModificable(factura);

        DetalleFactura entity = new DetalleFactura();
        entity.setIdFactura(dto.idFactura());
        entity.setConcepto(dto.concepto().trim());
        entity.setCantidad(dto.cantidad());
        entity.setPrecioUnitario(dto.precioUnitario());

        // RN-144: El importe deberá calcularse multiplicando cantidad por precio unitario
        BigDecimal importe = dto.precioUnitario().multiply(BigDecimal.valueOf(dto.cantidad()));
        entity.setImporte(importe);

        DetalleFactura guardado = detalleFacturaRepository.save(entity);

        // RN-145: Al modificar un detalle, deberán recalcularse los totales de la factura
        recalcularTotalesFactura(factura);

        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public DetalleFacturaDTO actualizar(DetalleFacturaDTO dto, Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID del detalle es obligatorio para la actualización");
        }

        DetalleFactura existente = detalleFacturaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el detalle de factura con ID: " + id));

        validarReglasDeNegocio(dto);

        Facturas facturaActual = facturasRepository.findById(existente.getIdFactura())
                .orElseThrow(() -> new ReglaNegocioException("RN-140: La factura asociada al detalle no existe."));

        // RN-146: No se pueden modificar detalles de facturas pagadas o canceladas
        validarFacturaModificable(facturaActual);

        // Si se intenta cambiar la factura a la que pertenece el detalle
        Facturas facturaNueva = null;
        if (!existente.getIdFactura().equals(dto.idFactura())) {
            facturaNueva = facturasRepository.findById(dto.idFactura())
                    .orElseThrow(() -> new ReglaNegocioException("RN-140: La nueva factura destino con ID " + dto.idFactura() + " no existe."));
            validarFacturaModificable(facturaNueva);
            existente.setIdFactura(dto.idFactura());
        }

        existente.setConcepto(dto.concepto().trim());
        existente.setCantidad(dto.cantidad());
        existente.setPrecioUnitario(dto.precioUnitario());

        // RN-144: El importe deberá calcularse multiplicando cantidad por precio unitario
        BigDecimal importe = dto.precioUnitario().multiply(BigDecimal.valueOf(dto.cantidad()));
        existente.setImporte(importe);

        DetalleFactura actualizado = detalleFacturaRepository.save(existente);

        // RN-145: Recalcular totales de la factura (o de ambas si cambió de factura)
        recalcularTotalesFactura(facturaActual);
        if (facturaNueva != null) {
            recalcularTotalesFactura(facturaNueva);
        }

        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID del detalle no puede ser nulo");
        }

        DetalleFactura existente = detalleFacturaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el detalle de factura con ID: " + id));

        Facturas factura = facturasRepository.findById(existente.getIdFactura())
                .orElseThrow(() -> new ReglaNegocioException("RN-140: La factura asociada no existe"));

        // RN-146: No se pueden eliminar detalles de facturas pagadas o canceladas
        validarFacturaModificable(factura);

        detalleFacturaRepository.deleteById(id);

        // RN-145: Recalcular totales de la factura tras la eliminación
        recalcularTotalesFactura(factura);
    }

    private void validarReglasDeNegocio(DetalleFacturaDTO dto) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos del detalle de factura son requeridos");
        }

        // RN-140: Todo detalle debe pertenecer a una factura existente
        if (dto.idFactura() == null) {
            throw new ReglaNegocioException("RN-140: Todo detalle debe pertenecer a una factura existente.");
        }

        // RN-141: El concepto facturado es obligatorio
        if (dto.concepto() == null || dto.concepto().trim().isEmpty()) {
            throw new ReglaNegocioException("RN-141: El concepto facturado es obligatorio.");
        }
        if (dto.concepto().trim().length() > 200) {
            throw new ReglaNegocioException("El concepto facturado no puede exceder los 200 caracteres.");
        }

        // RN-142: La cantidad debe ser mayor que cero
        if (dto.cantidad() == null || dto.cantidad() <= 0) {
            throw new ReglaNegocioException("RN-142: La cantidad debe ser mayor que cero.");
        }

        // RN-143: El precio unitario no puede ser negativo
        if (dto.precioUnitario() == null || dto.precioUnitario().compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("RN-143: El precio unitario no puede ser negativo.");
        }
    }

    private void validarFacturaModificable(Facturas factura) {
        if (ESTADO_FACTURA_PAGADA.equals(factura.getIdEstadoFactura())
                || ESTADO_FACTURA_CANCELADA.equals(factura.getIdEstadoFactura())) {
            throw new ReglaNegocioException("RN-146: No se podrán modificar detalles de facturas pagadas o canceladas mediante las operaciones ordinarias.");
        }
    }

    private void recalcularTotalesFactura(Facturas factura) {
        List<DetalleFactura> detalles = detalleFacturaRepository.findByIdFactura(factura.getIdFactura());
        BigDecimal nuevoSubtotal = detalles.stream()
                .map(DetalleFactura::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal iva = factura.getIva() != null ? factura.getIva() : BigDecimal.ZERO;
        BigDecimal nuevoTotal = nuevoSubtotal.add(iva);

        factura.setSubtotal(nuevoSubtotal);
        factura.setTotal(nuevoTotal);
        facturasRepository.save(factura);
    }
}
