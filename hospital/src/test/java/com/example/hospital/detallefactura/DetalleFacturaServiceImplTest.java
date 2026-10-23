package com.example.hospital.detallefactura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.detallefactura.dto.DetalleFacturaDTO;
import com.example.hospital.detallefactura.impl.DetalleFacturaServiceImpl;
import com.example.hospital.facturas.CalculoImportes;
import com.example.hospital.facturas.Facturas;
import com.example.hospital.facturas.FacturasRepository;
import com.example.hospital.pagos.Pagos;
import com.example.hospital.pagos.PagosRepository;
import com.example.hospital.shared.catalogos.EstadosCatalogo;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DetalleFacturaServiceImplTest {

    private static final Long PENDIENTE = 1L;
    private static final Long PAGADA = 2L;
    private static final Long CANCELADA = 3L;

    @Mock DetalleFacturaRepository detalleFacturaRepository;
    @Mock FacturasRepository facturasRepository;
    @Mock PagosRepository pagosRepository;
    @Mock EstadosCatalogo estados;
    @Spy CalculoImportes calculo = new CalculoImportes(new BigDecimal("0.12"));
    @InjectMocks DetalleFacturaServiceImpl service;

    @BeforeEach
    void catalogo() {
        lenient().when(estados.facturaPendiente()).thenReturn(PENDIENTE);
        lenient().when(detalleFacturaRepository.save(any(DetalleFactura.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Facturas factura(Long id, Long estado) {
        Facturas f = new Facturas();
        f.setIdFactura(id);
        f.setIdPaciente(1L);
        f.setSubtotal(BigDecimal.ZERO);
        f.setIva(BigDecimal.ZERO);
        f.setTotal(BigDecimal.ZERO);
        f.setIdEstadoFactura(estado);
        f.setActivo(true);
        return f;
    }

    private DetalleFactura linea(Long id, Long idFactura, String importe) {
        DetalleFactura d = new DetalleFactura();
        d.setIdDetalle(id);
        d.setIdFactura(idFactura);
        d.setConcepto("c");
        d.setCantidad(1);
        d.setPrecioUnitario(new BigDecimal(importe));
        d.setImporte(new BigDecimal(importe));
        return d;
    }

    private DetalleFacturaDTO dto(Long idFactura, String concepto, int cantidad, String precio) {
        // el cliente intenta fijar el importe: se ignora
        return new DetalleFacturaDTO(null, idFactura, concepto, cantidad, new BigDecimal(precio), new BigDecimal("1.00"));
    }

    @Test
    void crear_calculaImporte_yRecalculaSubtotalIvaYTotalDeLaFactura() {
        Facturas f = factura(1L, PENDIENTE);
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(f));
        when(detalleFacturaRepository.findByIdFactura(1L)).thenReturn(List.of(linea(1L, 1L, "150.00"), linea(2L, 1L, "37.50")));
        when(pagosRepository.findByIdFactura(1L)).thenReturn(List.of());

        DetalleFacturaDTO r = service.crear(dto(1L, "  Consulta  ", 3, "12.50"));

        assertThat(r.importe()).isEqualByComparingTo("37.50");
        assertThat(r.concepto()).isEqualTo("Consulta");
        assertThat(f.getSubtotal()).isEqualByComparingTo("187.50");
        assertThat(f.getIva()).isEqualByComparingTo("22.50");
        assertThat(f.getTotal()).isEqualByComparingTo("210.00");
        verify(facturasRepository).save(f);
    }

    @Test
    void ivaSeRedondeaAlCentavo_totalEsSubtotalMasIva() {
        Facturas f = factura(1L, PENDIENTE);
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(f));
        when(detalleFacturaRepository.findByIdFactura(1L)).thenReturn(List.of(linea(1L, 1L, "0.30")));
        when(pagosRepository.findByIdFactura(1L)).thenReturn(List.of());

        service.crear(dto(1L, "x", 3, "0.10"));

        assertThat(f.getIva()).isEqualByComparingTo("0.04");
        assertThat(f.getTotal()).isEqualByComparingTo("0.34");
    }

    @Test
    void crear_lanza422_enFacturaPagadaCanceladaOAnulada_yNoGuarda() {
        Facturas anulada = factura(4L, PENDIENTE);
        anulada.setActivo(false);
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(factura(1L, PAGADA)));
        when(facturasRepository.findById(2L)).thenReturn(Optional.of(factura(2L, CANCELADA)));
        when(facturasRepository.findById(4L)).thenReturn(Optional.of(anulada));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(1L, "x", 1, "1")));
        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(2L, "x", 1, "1")));
        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(4L, "x", 1, "1")));

        verify(detalleFacturaRepository, never()).save(any());
    }

    @Test
    void crear_lanza422_siLaFacturaNoExisteEnElCuerpo() {
        when(facturasRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(99L, "x", 1, "1")));
    }

    @Test
    void crear_lanza422_siElTotalQuedariaPorDebajoDeLoPagado() {
        Facturas f = factura(1L, PENDIENTE);
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(f));
        when(detalleFacturaRepository.findByIdFactura(1L)).thenReturn(List.of(linea(1L, 1L, "10.00")));
        Pagos p = new Pagos();
        p.setMonto(new BigDecimal("500.00"));
        p.setActivo(true);
        when(pagosRepository.findByIdFactura(1L)).thenReturn(List.of(p));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(1L, "x", 1, "10")));

        verify(facturasRepository, never()).save(any());
    }

    @Test
    void crear_lanza422_siElImporteSuperaElLimiteDeDecimal10_2() {
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(factura(1L, PENDIENTE)));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto(1L, "x", 100000, "99999.99")));

        verify(detalleFacturaRepository, never()).save(any());
    }

    @Test
    void actualizar_noPermiteMoverLaLineaAOtraFactura() {
        when(detalleFacturaRepository.findById(5L)).thenReturn(Optional.of(linea(5L, 1L, "10.00")));

        assertThrows(ReglaNegocioException.class, () -> service.actualizar(dto(2L, "x", 1, "1"), 5L));

        verify(detalleFacturaRepository, never()).save(any());
    }

    @Test
    void actualizar_recalculaLosTotales_yDa422EnFacturaPagada() {
        DetalleFactura l = linea(5L, 1L, "10.00");
        Facturas pendiente = factura(1L, PENDIENTE);
        when(detalleFacturaRepository.findById(5L)).thenReturn(Optional.of(l));
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(pendiente));
        when(detalleFacturaRepository.findByIdFactura(1L)).thenReturn(List.of(l));
        when(pagosRepository.findByIdFactura(1L)).thenReturn(List.of());

        DetalleFacturaDTO r = service.actualizar(dto(1L, "x", 2, "100.00"), 5L);

        assertThat(r.importe()).isEqualByComparingTo("200.00");
        assertThat(pendiente.getSubtotal()).isEqualByComparingTo("200.00");
        assertThat(pendiente.getTotal()).isEqualByComparingTo("224.00");

        // factura pagada: 422
        Facturas pagada = factura(2L, PAGADA);
        DetalleFactura l2 = linea(6L, 2L, "10.00");
        when(detalleFacturaRepository.findById(6L)).thenReturn(Optional.of(l2));
        when(facturasRepository.findById(2L)).thenReturn(Optional.of(pagada));
        assertThrows(ReglaNegocioException.class, () -> service.actualizar(dto(2L, "x", 1, "1"), 6L));
    }

    @Test
    void eliminar_recalculaLosTotales_yDa422EnFacturaPagadaOCancelada() {
        DetalleFactura l = linea(5L, 1L, "10.00");
        Facturas pendiente = factura(1L, PENDIENTE);
        when(detalleFacturaRepository.findById(5L)).thenReturn(Optional.of(l));
        when(facturasRepository.findById(1L)).thenReturn(Optional.of(pendiente));
        when(detalleFacturaRepository.findByIdFactura(1L)).thenReturn(List.of());
        when(pagosRepository.findByIdFactura(1L)).thenReturn(List.of());

        service.eliminar(5L);

        verify(detalleFacturaRepository).deleteById(5L);
        assertThat(pendiente.getTotal()).isEqualByComparingTo("0");

        DetalleFactura l2 = linea(6L, 2L, "10.00");
        DetalleFactura l3 = linea(7L, 3L, "10.00");
        when(detalleFacturaRepository.findById(6L)).thenReturn(Optional.of(l2));
        when(detalleFacturaRepository.findById(7L)).thenReturn(Optional.of(l3));
        when(facturasRepository.findById(2L)).thenReturn(Optional.of(factura(2L, PAGADA)));
        when(facturasRepository.findById(3L)).thenReturn(Optional.of(factura(3L, CANCELADA)));
        assertThrows(ReglaNegocioException.class, () -> service.eliminar(6L));
        assertThrows(ReglaNegocioException.class, () -> service.eliminar(7L));
        verify(detalleFacturaRepository, never()).deleteById(6L);
        verify(detalleFacturaRepository, never()).deleteById(7L);
    }

    @Test
    void actualizarYEliminar_lanzan404_siLaLineaNoExiste() {
        when(detalleFacturaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizar(dto(1L, "x", 1, "1"), 99L));
        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(99L));
    }
}
