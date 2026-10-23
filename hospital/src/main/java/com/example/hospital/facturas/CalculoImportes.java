package com.example.hospital.facturas;

import com.example.hospital.pagos.Pagos;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Cálculos de importes de facturación (todo con escala 2, como DECIMAL(10,2)):
 * - el IVA lo calcula el servidor: subtotal x tasa configurable (`app.facturacion.iva`, por defecto 0.12), redondeo
 *   al centavo (HALF_UP);
 * - total = subtotal + IVA (respeta el CHECK de la tabla `facturas`);
 * - lo pagado es la suma de los pagos ACTIVOS.
 */
@Component
public class CalculoImportes {

    private final BigDecimal tasaIva;

    public CalculoImportes(@Value("${app.facturacion.iva:0.12}") BigDecimal tasaIva) {
        this.tasaIva = tasaIva;
    }

    public BigDecimal tasaIva() {
        return tasaIva;
    }

    public BigDecimal iva(BigDecimal subtotal) {
        return subtotal.multiply(tasaIva).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal total(BigDecimal subtotal, BigDecimal iva) {
        return subtotal.add(iva).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal pagado(List<Pagos> pagos) {
        return pagos.stream()
                .filter(p -> !Boolean.FALSE.equals(p.getActivo()))
                .map(p -> p.getMonto() != null ? p.getMonto() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Saldo pendiente = total - pagado (nunca negativo). */
    public BigDecimal pendiente(BigDecimal total, BigDecimal pagado) {
        BigDecimal pendiente = (total != null ? total : BigDecimal.ZERO).subtract(pagado);
        return pendiente.signum() < 0 ? BigDecimal.ZERO.setScale(2) : pendiente.setScale(2, RoundingMode.HALF_UP);
    }
}
