package com.example.hospital.facturas.impl;

import com.example.hospital.estadosfactura.EstadosFactura;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.estadosfactura.EstadosFacturaRepository;
import com.example.hospital.facturas.Facturas;
import com.example.hospital.facturas.FacturasRepository;
import com.example.hospital.facturas.FacturasService;
import com.example.hospital.facturas.dto.FacturasDTO;
import com.example.hospital.facturas.dto.SaldoFacturaDTO;
import com.example.hospital.pagos.Pagos;
import com.example.hospital.pagos.PagosRepository;
import com.example.hospital.hospitalizaciones.Hospitalizaciones;
import com.example.hospital.hospitalizaciones.HospitalizacionesRepository;
import com.example.hospital.metodospago.MetodosPago;
import com.example.hospital.metodospago.MetodosPagoRepository;
import com.example.hospital.paciente.Paciente;
import com.example.hospital.paciente.PacienteRepository;
import com.example.hospital.shared.exception.ReglaNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FacturasServiceImpl implements FacturasService {

    public static final Long ESTADO_FACTURA_PENDIENTE = 1L;
    public static final Long ESTADO_FACTURA_PAGADA = 2L;
    public static final Long ESTADO_FACTURA_CANCELADA = 3L;

    private final FacturasRepository facturasRepository;
    private final PacienteRepository pacienteRepository;
    private final HospitalizacionesRepository hospitalizacionesRepository;
    private final MetodosPagoRepository metodosPagoRepository;
    private final EstadosFacturaRepository estadosFacturaRepository;
    private final PagosRepository pagosRepository;

    public FacturasServiceImpl(FacturasRepository facturasRepository,
                               PacienteRepository pacienteRepository,
                               HospitalizacionesRepository hospitalizacionesRepository,
                               MetodosPagoRepository metodosPagoRepository,
                               EstadosFacturaRepository estadosFacturaRepository,
                               PagosRepository pagosRepository) {
        this.facturasRepository = facturasRepository;
        this.pacienteRepository = pacienteRepository;
        this.hospitalizacionesRepository = hospitalizacionesRepository;
        this.metodosPagoRepository = metodosPagoRepository;
        this.estadosFacturaRepository = estadosFacturaRepository;
        this.pagosRepository = pagosRepository;
    }

    private FacturasDTO mapToDTO(Facturas f) {
        return new FacturasDTO(
                f.getIdFactura(),
                f.getIdPaciente(),
                f.getIdHospitalizacion(),
                f.getFechaEmision(),
                f.getSubtotal(),
                f.getIva(),
                f.getTotal(),
                f.getIdMetodoPago(),
                f.getIdEstadoFactura(),
                f.getActivo()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturasDTO> findAll() {
        return facturasRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturasDTO> findActivas() {
        return facturasRepository.findByActivoTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FacturasDTO findById(Long id) {
        Facturas entity = facturasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la factura con ID: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturasDTO> findByPaciente(Long idPaciente) {
        if (idPaciente == null) {
            throw new ReglaNegocioException("El ID del paciente es requerido");
        }
        return facturasRepository.findByIdPacienteAndActivoTrue(idPaciente)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturasDTO> findByHospitalizacion(Long idHospitalizacion) {
        if (idHospitalizacion == null) {
            throw new ReglaNegocioException("El ID de hospitalización es requerido");
        }
        return facturasRepository.findByIdHospitalizacionAndActivoTrue(idHospitalizacion)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FacturasDTO crear(FacturasDTO dto) {
        validarReglasDeNegocio(dto, null);

        Facturas entity = new Facturas();
        entity.setIdPaciente(dto.idPaciente());
        entity.setIdHospitalizacion(dto.idHospitalizacion());

        // Fecha de emisión asignada automáticamente si no se envía
        LocalDateTime fecha = dto.fechaEmision() != null ? dto.fechaEmision() : LocalDateTime.now();
        entity.setFechaEmision(fecha);

        // RN-134, RN-135, RN-136: Cálculo consistente de importes
        BigDecimal subtotal = dto.subtotal();
        BigDecimal iva = dto.iva() != null ? dto.iva() : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(iva);

        entity.setSubtotal(subtotal);
        entity.setIva(iva);
        entity.setTotal(total);

        entity.setIdMetodoPago(dto.idMetodoPago());
        entity.setIdEstadoFactura(dto.idEstadoFactura());
        entity.setActivo(dto.activo() != null ? dto.activo() : true);

        Facturas guardada = facturasRepository.save(entity);
        return mapToDTO(guardada);
    }

    @Override
    @Transactional
    public FacturasDTO actualizar(FacturasDTO dto, Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID de la factura es obligatorio para la actualización");
        }

        Facturas existente = facturasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la factura con ID: " + id));

        validarReglasDeNegocio(dto, id);

        // RN-139: No se podrán modificar los importes de una factura pagada o cancelada mediante las operaciones ordinarias
        boolean esPagadaOCancelada = ESTADO_FACTURA_PAGADA.equals(existente.getIdEstadoFactura())
                || ESTADO_FACTURA_CANCELADA.equals(existente.getIdEstadoFactura());

        BigDecimal nuevoSubtotal = dto.subtotal();
        BigDecimal nuevoIva = dto.iva() != null ? dto.iva() : BigDecimal.ZERO;
        BigDecimal nuevoTotal = nuevoSubtotal.add(nuevoIva);

        if (esPagadaOCancelada) {
            if (existente.getSubtotal().compareTo(nuevoSubtotal) != 0
                    || existente.getIva().compareTo(nuevoIva) != 0) {
                throw new ReglaNegocioException("RN-139: No se podrán modificar los importes de una factura pagada o cancelada mediante las operaciones ordinarias.");
            }

            if (!existente.getIdPaciente().equals(dto.idPaciente())
                    || !Objects.equals(existente.getIdHospitalizacion(), dto.idHospitalizacion())) {
                throw new ReglaNegocioException("No se permite modificar el paciente ni la hospitalización de una factura pagada o cancelada.");
            }
        } else {
            existente.setSubtotal(nuevoSubtotal);
            existente.setIva(nuevoIva);
            existente.setTotal(nuevoTotal);
            existente.setIdPaciente(dto.idPaciente());
            existente.setIdHospitalizacion(dto.idHospitalizacion());
        }

        if (dto.fechaEmision() != null) {
            existente.setFechaEmision(dto.fechaEmision());
        }
        existente.setIdMetodoPago(dto.idMetodoPago());
        existente.setIdEstadoFactura(dto.idEstadoFactura());
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        Facturas actualizada = facturasRepository.save(existente);
        return mapToDTO(actualizada);
    }

    @Override
    @Transactional
    public FacturasDTO cambiarEstado(Long id, Long nuevoEstadoId) {
        if (id == null || nuevoEstadoId == null) {
            throw new ReglaNegocioException("El ID de factura y el nuevo estado son requeridos");
        }

        Facturas existente = facturasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la factura con ID: " + id));

        EstadosFactura nuevoEstado = estadosFacturaRepository.findById(nuevoEstadoId)
                .orElseThrow(() -> new ReglaNegocioException("El estado de factura " + nuevoEstadoId + " no existe"));

        if (!Boolean.TRUE.equals(nuevoEstado.getActivo())) {
            throw new ReglaNegocioException("El estado de factura seleccionado se encuentra inactivo");
        }

        existente.setIdEstadoFactura(nuevoEstadoId);
        Facturas guardada = facturasRepository.save(existente);
        return mapToDTO(guardada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("El ID de la factura no puede ser nulo");
        }

        Facturas existente = facturasRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la factura con ID: " + id));

        // Protección contable: no eliminar facturas pagadas
        if (ESTADO_FACTURA_PAGADA.equals(existente.getIdEstadoFactura())) {
            throw new ReglaNegocioException("No se puede eliminar una factura que ya ha sido Pagada.");
        }

        // Borrado lógico sistemático
        existente.setActivo(false);
        facturasRepository.save(existente);
    }

    private void validarReglasDeNegocio(FacturasDTO dto, Long idActualizacion) {
        if (dto == null) {
            throw new ReglaNegocioException("Los datos de la factura son requeridos");
        }

        // RN-132: Toda factura debe pertenecer a un paciente existente
        if (dto.idPaciente() == null) {
            throw new ReglaNegocioException("RN-132: Toda factura debe pertenecer a un paciente existente.");
        }
        Paciente paciente = pacienteRepository.findById(dto.idPaciente())
                .orElseThrow(() -> new ReglaNegocioException("RN-132: El paciente con ID " + dto.idPaciente() + " no existe"));
        if (!Boolean.TRUE.equals(paciente.getActivo())) {
            throw new ReglaNegocioException("El paciente seleccionado se encuentra inactivo");
        }

        // RN-133: Si se asocia una hospitalización, esta debe pertenecer al mismo paciente de la factura
        if (dto.idHospitalizacion() != null) {
            Hospitalizaciones hospitalizacion = hospitalizacionesRepository.findById(dto.idHospitalizacion())
                    .orElseThrow(() -> new ReglaNegocioException("La hospitalización con ID " + dto.idHospitalizacion() + " no existe"));

            if (!dto.idPaciente().equals(hospitalizacion.getIdPaciente())) {
                throw new ReglaNegocioException("RN-133: La hospitalización asociada (" + dto.idHospitalizacion()
                        + ") no pertenece al mismo paciente (" + dto.idPaciente() + ") de la factura.");
            }
        }

        // RN-134: El subtotal no puede ser negativo
        if (dto.subtotal() == null || dto.subtotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("RN-134: El subtotal no puede ser negativo.");
        }

        // RN-135: El impuesto no puede ser negativo
        BigDecimal iva = dto.iva() != null ? dto.iva() : BigDecimal.ZERO;
        if (iva.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("RN-135: El impuesto no puede ser negativo.");
        }

        // RN-137: El método de pago debe existir
        if (dto.idMetodoPago() == null) {
            throw new ReglaNegocioException("RN-137: El método de pago es obligatorio.");
        }
        MetodosPago metodoPago = metodosPagoRepository.findById(dto.idMetodoPago())
                .orElseThrow(() -> new ReglaNegocioException("RN-137: El método de pago con ID " + dto.idMetodoPago() + " no existe."));
        if (!Boolean.TRUE.equals(metodoPago.getActivo())) {
            throw new ReglaNegocioException("El método de pago seleccionado se encuentra inactivo.");
        }

        // RN-138: Toda factura debe tener un estado válido
        if (dto.idEstadoFactura() == null) {
            throw new ReglaNegocioException("RN-138: Toda factura debe tener un estado válido.");
        }
        EstadosFactura estadoFactura = estadosFacturaRepository.findById(dto.idEstadoFactura())
                .orElseThrow(() -> new ReglaNegocioException("RN-138: El estado de factura con ID " + dto.idEstadoFactura() + " no existe."));
        if (!Boolean.TRUE.equals(estadoFactura.getActivo())) {
            throw new ReglaNegocioException("El estado de factura seleccionado se encuentra inactivo.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SaldoFacturaDTO obtenerSaldo(Long idFactura) {
        Facturas factura = facturasRepository.findById(idFactura)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la factura con ID: " + idFactura));

        BigDecimal total = factura.getTotal() != null ? factura.getTotal() : BigDecimal.ZERO;
        List<Pagos> pagos = pagosRepository.findByIdFactura(idFactura)
                .stream()
                .filter(p -> !Boolean.FALSE.equals(p.getActivo()))
                .collect(Collectors.toList());
        BigDecimal pagado = pagos.stream()
                .map(p -> p.getMonto() != null ? p.getMonto() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendiente = total.subtract(pagado);
        if (pendiente.compareTo(BigDecimal.ZERO) < 0) {
            pendiente = BigDecimal.ZERO;
        }

        return new SaldoFacturaDTO(
                factura.getIdFactura(),
                factura.getIdPaciente(),
                total,
                pagado,
                pendiente,
                pagos.size(),
                pendiente.compareTo(BigDecimal.ZERO) == 0
        );
    }
}
