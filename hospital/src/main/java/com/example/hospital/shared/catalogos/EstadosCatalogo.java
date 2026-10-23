package com.example.hospital.shared.catalogos;

import com.example.hospital.estadohabitacion.EstadoHabitacionRepository;
import com.example.hospital.estadoshospitalizacion.EstadosHospitalizacionRepository;
import com.example.hospital.estadosfactura.EstadosFacturaRepository;
import com.example.hospital.shared.exception.ReglaNegocioException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Resuelve por descripción (y no por id fijo) los estados que usa la lógica de hospitalizaciones y
 * habitaciones. El id encontrado se guarda en una caché simple; si el catálogo no contiene el estado
 * esperado se responde 422 con un mensaje claro.
 */
@Component
public class EstadosCatalogo {

    public static final String HOSP_ACTIVA = "Activa";
    public static final String HOSP_ALTA = "Alta";
    public static final String FACTURA_PENDIENTE = "Pendiente";
    public static final String FACTURA_PAGADA = "Pagada";
    public static final String FACTURA_CANCELADA = "Cancelada";
    public static final String HAB_DISPONIBLE = "Disponible";
    public static final String HAB_OCUPADA = "Ocupada";
    public static final String HAB_MANTENIMIENTO = "Mantenimiento";

    private final EstadosHospitalizacionRepository estadosHospitalizacionRepository;
    private final EstadoHabitacionRepository estadoHabitacionRepository;
    private final EstadosFacturaRepository estadosFacturaRepository;
    private final Map<String, Long> cache = new ConcurrentHashMap<>();

    public EstadosCatalogo(EstadosHospitalizacionRepository estadosHospitalizacionRepository,
                           EstadoHabitacionRepository estadoHabitacionRepository,
                           EstadosFacturaRepository estadosFacturaRepository) {
        this.estadosHospitalizacionRepository = estadosHospitalizacionRepository;
        this.estadoHabitacionRepository = estadoHabitacionRepository;
        this.estadosFacturaRepository = estadosFacturaRepository;
    }

    public Long hospActiva() {
        return hospitalizacion(HOSP_ACTIVA);
    }

    public Long hospAlta() {
        return hospitalizacion(HOSP_ALTA);
    }

    public Long habDisponible() {
        return habitacion(HAB_DISPONIBLE);
    }

    public Long habOcupada() {
        return habitacion(HAB_OCUPADA);
    }

    public Long habMantenimiento() {
        return habitacion(HAB_MANTENIMIENTO);
    }

    public Long facturaPendiente() {
        return factura(FACTURA_PENDIENTE);
    }

    public Long facturaPagada() {
        return factura(FACTURA_PAGADA);
    }

    public Long facturaCancelada() {
        return factura(FACTURA_CANCELADA);
    }

    private Long factura(String descripcion) {
        return cache.computeIfAbsent("fact:" + descripcion, k ->
                estadosFacturaRepository.findByDescripcionIgnoreCase(descripcion)
                        .orElseThrow(() -> new ReglaNegocioException(
                                "El catálogo de estados de factura no contiene el estado '" + descripcion + "'"))
                        .getIdEstadoFactura());
    }

    private Long hospitalizacion(String descripcion) {
        return cache.computeIfAbsent("hosp:" + descripcion, k ->
                estadosHospitalizacionRepository.findByDescripcionIgnoreCase(descripcion)
                        .orElseThrow(() -> new ReglaNegocioException(
                                "El catálogo de estados de hospitalización no contiene el estado '" + descripcion + "'"))
                        .getIdEstadoHosp());
    }

    private Long habitacion(String descripcion) {
        return cache.computeIfAbsent("hab:" + descripcion, k ->
                estadoHabitacionRepository.findByDescripcionIgnoreCase(descripcion)
                        .orElseThrow(() -> new ReglaNegocioException(
                                "El catálogo de estados de habitación no contiene el estado '" + descripcion + "'"))
                        .getIdEstadoHabitacion());
    }
}
