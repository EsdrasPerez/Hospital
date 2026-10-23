package com.example.hospital.detallefactura.impl;

import com.example.hospital.detallefactura.DetalleFactura;
import com.example.hospital.detallefactura.DetalleFacturaRepository;
import com.example.hospital.detallefactura.DetalleFacturaService;
import com.example.hospital.detallefactura.dto.DetalleFacturaDTO;
import com.example.hospital.facturas.CalculoImportes;
import com.example.hospital.facturas.Facturas;
import com.example.hospital.facturas.FacturasRepository;
import com.example.hospital.pagos.PagosRepository;
import com.example.hospital.shared.catalogos.EstadosCatalogo;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DetalleFacturaServiceImpl implements DetalleFacturaService {

    // Límite de DECIMAL(10,2)
    private static final BigDecimal MAXIMO = new BigDecimal("99999999.99");

    private final DetalleFacturaRepository detalleFacturaRepository;
    private final FacturasRepository facturasRepository;
    private final PagosRepository pagosRepository;
    private final EstadosCatalogo estados;
    private final CalculoImportes calculo;

    public DetalleFacturaServiceImpl(DetalleFacturaRepository detalleFacturaRepository,
                                     FacturasRepository facturasRepository,
                                     PagosRepository pagosRepository,
                                     EstadosCatalogo estados,
                                     CalculoImportes calculo) {
        this.detalleFacturaRepository = detalleFacturaRepository;
        this.facturasRepository = facturasRepository;
        this.pagosRepository = pagosRepository;
        this.estados = estados;
        this.calculo = calculo;
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
        return detalleFacturaRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
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
        return detalleFacturaRepository.findByIdFactura(idFactura).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DetalleFacturaDTO crear(DetalleFacturaDTO dto) {
        validar(dto);

        // RN-140: la factura debe existir (referencia del cuerpo: 422) y estar activa
        Facturas factura = facturasRepository.findById(dto.idFactura())
                .orElseThrow(() -> new ReglaNegocioException("RN-140: La factura con ID " + dto.idFactura() + " no existe."));
        validarFacturaModificable(factura);

        DetalleFactura entity = new DetalleFactura();
        entity.setIdFactura(dto.idFactura());
        aplicar(entity, dto);
        DetalleFactura guardado = detalleFacturaRepository.save(entity);

        // RN-145: al modificar un detalle se recalculan los totales de la factura
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
        validar(dto);

        // Una línea no se puede mover a otra factura
        if (!Objects.equals(existente.getIdFactura(), dto.idFactura())) {
            throw new ReglaNegocioException("No se puede mover una línea a otra factura: elimínela y agréguela en la factura destino.");
        }
        Facturas factura = facturasRepository.findById(existente.getIdFactura())
                .orElseThrow(() -> new ReglaNegocioException("RN-140: La factura asociada al detalle no existe."));
        // RN-146: no se modifican líneas de facturas pagadas, canceladas o anuladas
        validarFacturaModificable(factura);

        aplicar(existente, dto);
        DetalleFactura actualizado = detalleFacturaRepository.save(existente);
        recalcularTotalesFactura(factura);
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
        // RN-146
        validarFacturaModificable(factura);

        detalleFacturaRepository.deleteById(id);
        recalcularTotalesFactura(factura);
    }

    /** Aplica concepto, cantidad, precio y el importe (RN-144: cantidad x precio unitario) a la entidad. */
    private void aplicar(DetalleFactura entity, DetalleFacturaDTO dto) {
        BigDecimal importe = dto.precioUnitario().multiply(BigDecimal.valueOf(dto.cantidad()));
        if (importe.compareTo(MAXIMO) > 0) {
            throw new ReglaNegocioException("El importe de la línea (cantidad x precio) supera el máximo permitido de 99,999,999.99.");
        }
        entity.setConcepto(dto.concepto().trim());
        entity.setCantidad(dto.cantidad());
        entity.setPrecioUnitario(dto.precioUnitario());
        entity.setImporte(importe);
    }

    // Formato, obligatoriedad y tamaños los valida Bean Validation (400); esto cubre llamadas internas
    private void validar(DetalleFacturaDTO dto) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos del detalle de factura son requeridos");
        }
        if (dto.idFactura() == null) {
            throw new ReglaNegocioException("RN-140: Todo detalle debe pertenecer a una factura existente.");
        }
        if (dto.concepto() == null || dto.concepto().trim().isEmpty()) {
            throw new ReglaNegocioException("RN-141: El concepto facturado es obligatorio.");
        }
        if (dto.cantidad() == null || dto.cantidad() <= 0) {
            throw new ReglaNegocioException("RN-142: La cantidad debe ser mayor que cero.");
        }
        if (dto.precioUnitario() == null || dto.precioUnitario().signum() < 0) {
            throw new ReglaNegocioException("RN-143: El precio unitario no puede ser negativo.");
        }
    }

    // RN-146: solo se modifican líneas de facturas activas y Pendientes
    private void validarFacturaModificable(Facturas factura) {
        if (!Boolean.TRUE.equals(factura.getActivo())) {
            throw new ReglaNegocioException("La factura seleccionada se encuentra anulada.");
        }
        if (!Objects.equals(factura.getIdEstadoFactura(), estados.facturaPendiente())) {
            throw new ReglaNegocioException("RN-146: No se podrán modificar detalles de facturas pagadas o canceladas mediante las operaciones ordinarias.");
        }
    }

    /**
     * El subtotal sale SOLO de las líneas; el IVA lo calcula el servidor (subtotal x tasa configurada) y
     * total = subtotal + IVA. Si la factura ya tiene pagos activos, el nuevo total no puede quedar por debajo
     * de lo pagado (422 y se deshace el cambio).
     */
    private void recalcularTotalesFactura(Facturas factura) {
        BigDecimal subtotal = detalleFacturaRepository.findByIdFactura(factura.getIdFactura()).stream()
                .map(DetalleFactura::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (subtotal.compareTo(MAXIMO) > 0) {
            throw new ReglaNegocioException("El subtotal de la factura supera el máximo permitido de 99,999,999.99.");
        }
        BigDecimal iva = calculo.iva(subtotal);
        BigDecimal total = calculo.total(subtotal, iva);
        if (total.compareTo(MAXIMO) > 0) {
            throw new ReglaNegocioException("El total de la factura supera el máximo permitido de 99,999,999.99.");
        }
        BigDecimal pagado = calculo.pagado(pagosRepository.findByIdFactura(factura.getIdFactura()));
        if (total.compareTo(pagado) < 0) {
            throw new ReglaNegocioException("El cambio dejaría el total de la factura (" + total
                    + ") por debajo de lo ya pagado (" + pagado + "): anule primero los pagos necesarios.");
        }
        factura.setSubtotal(subtotal.setScale(2));
        factura.setIva(iva);
        factura.setTotal(total);
        facturasRepository.save(factura);
    }
}
