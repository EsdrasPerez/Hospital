package com.example.hospital.pagos.impl;

import com.example.hospital.pagos.Pagos;
import com.example.hospital.pagos.PagosRepository;
import com.example.hospital.pagos.PagosService;
import com.example.hospital.pagos.dto.PagosDTO;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.facturas.Facturas;
import com.example.hospital.facturas.FacturasRepository;
import com.example.hospital.metodospago.MetodosPago;
import com.example.hospital.metodospago.MetodosPagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PagosServiceImpl implements PagosService {

    private final PagosRepository pagosRepository;
    private final FacturasRepository facturasRepository;
    private final MetodosPagoRepository metodosPagoRepository;

    public PagosServiceImpl(PagosRepository pagosRepository,
                            FacturasRepository facturasRepository,
                            MetodosPagoRepository metodosPagoRepository) {
        this.pagosRepository = pagosRepository;
        this.facturasRepository = facturasRepository;
        this.metodosPagoRepository = metodosPagoRepository;
    }

    private PagosDTO mapToDTO(Pagos e) {
        return new PagosDTO(
                e.getIdPago(),
                e.getIdFactura(),
                e.getIdCuenta(),
                e.getIdMetodoPago(),
                e.getMonto(),
                e.getFechaPago(),
                e.getReferencia(),
                e.getActivo()
        );
    }

    private void validar(PagosDTO dto, Long idActual) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos de el pago son requeridos");
        }
        if (dto.idFactura() == null) {
            throw new ReglaNegocioException("La factura del pago es obligatoria");
        }
        Facturas factura = facturasRepository.findById(dto.idFactura())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la factura con ID: " + dto.idFactura()));
        if (Boolean.FALSE.equals(factura.getActivo())) {
            throw new ReglaNegocioException("La factura asociada se encuentra inactiva");
        }
        if (dto.idMetodoPago() == null) {
            throw new ReglaNegocioException("El método de pago es obligatorio");
        }
        MetodosPago metodo = metodosPagoRepository.findById(dto.idMetodoPago())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el método de pago con ID: " + dto.idMetodoPago()));
        if (Boolean.FALSE.equals(metodo.getActivo())) {
            throw new ReglaNegocioException("El método de pago seleccionado se encuentra inactivo");
        }
        if (dto.monto() == null || dto.monto().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El monto del pago debe ser mayor que 0");
        }
        if (dto.fechaPago() != null && dto.fechaPago().isAfter(java.time.LocalDateTime.now())) {
            throw new ReglaNegocioException("La fecha de pago no puede ser futura");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagosDTO> findAll() {
        return pagosRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagosDTO findById(Long id) {
        return mapToDTO(buscar(id));
    }

    @Override
    @Transactional
    public PagosDTO crear(PagosDTO dto) {
        validar(dto, null);
        Pagos entity = new Pagos();
        entity.setIdFactura(dto.idFactura());
        entity.setIdCuenta(dto.idCuenta());
        entity.setIdMetodoPago(dto.idMetodoPago());
        entity.setMonto(dto.monto());
        entity.setFechaPago(dto.fechaPago() != null ? dto.fechaPago() : java.time.LocalDateTime.now());
        entity.setReferencia(dto.referencia());
        entity.setActivo(dto.activo() != null ? dto.activo() : true);
        return mapToDTO(pagosRepository.save(entity));
    }

    @Override
    @Transactional
    public PagosDTO actualizar(PagosDTO dto, Long id) {
        Pagos existente = buscar(id);
        validar(dto, id);
        Pagos entity = existente;
        entity.setIdFactura(dto.idFactura());
        entity.setIdCuenta(dto.idCuenta());
        entity.setIdMetodoPago(dto.idMetodoPago());
        entity.setMonto(dto.monto());
        if (dto.fechaPago() != null) {
            entity.setFechaPago(dto.fechaPago());
        }
        entity.setReferencia(dto.referencia());
        if (dto.activo() != null) {
            entity.setActivo(dto.activo());
        }
        return mapToDTO(pagosRepository.save(entity));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Pagos existente = buscar(id);
        // Eliminación lógica para conservar el historial
        existente.setActivo(false);
        pagosRepository.save(existente);
    }

    private Pagos buscar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID de el pago es obligatorio");
        }
        return pagosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pago con ID: " + id));
    }
}
