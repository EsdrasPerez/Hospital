package com.example.hospital.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.estadohabitacion.EstadoHabitacion;
import com.example.hospital.estadohabitacion.EstadoHabitacionRepository;
import com.example.hospital.estadosfactura.EstadosFactura;
import com.example.hospital.estadosfactura.EstadosFacturaRepository;
import com.example.hospital.estadoshospitalizacion.EstadosHospitalizacion;
import com.example.hospital.estadoshospitalizacion.EstadosHospitalizacionRepository;
import com.example.hospital.shared.catalogos.EstadosCatalogo;
import com.example.hospital.shared.exception.ReglaNegocioException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EstadosCatalogoTest {

    @Mock EstadosHospitalizacionRepository hospRepository;
    @Mock EstadoHabitacionRepository habRepository;
    @Mock EstadosFacturaRepository facturaRepository;
    @InjectMocks EstadosCatalogo catalogo;

    @Test
    void resuelvePorNombre_yGuardaElIdEnCache() {
        EstadosHospitalizacion activa = new EstadosHospitalizacion();
        activa.setIdEstadoHosp(10L);
        when(hospRepository.findByDescripcionIgnoreCase("Activa")).thenReturn(Optional.of(activa));

        assertThat(catalogo.hospActiva()).isEqualTo(10L);
        assertThat(catalogo.hospActiva()).isEqualTo(10L);

        verify(hospRepository, times(1)).findByDescripcionIgnoreCase("Activa");
    }

    @Test
    void estadosDeHabitacion_seBuscanPorDescripcion() {
        EstadoHabitacion mant = new EstadoHabitacion();
        mant.setIdEstadoHabitacion(7L);
        when(habRepository.findByDescripcionIgnoreCase("Mantenimiento")).thenReturn(Optional.of(mant));

        assertThat(catalogo.habMantenimiento()).isEqualTo(7L);
    }

    @Test
    void siElCatalogoNoTieneElEstado_lanzaReglaNegocio422() {
        when(hospRepository.findByDescripcionIgnoreCase("Alta")).thenReturn(Optional.empty());
        when(habRepository.findByDescripcionIgnoreCase("Ocupada")).thenReturn(Optional.empty());

        assertThrows(ReglaNegocioException.class, () -> catalogo.hospAlta());
        assertThrows(ReglaNegocioException.class, () -> catalogo.habOcupada());
    }

    @Test
    void estadosDeFactura_seBuscanPorNombre_yLanzan422SiFaltan() {
        when(facturaRepository.findByDescripcionIgnoreCase("Pendiente")).thenReturn(Optional.of(new EstadosFactura(11L, "Pendiente", true)));
        when(facturaRepository.findByDescripcionIgnoreCase("Pagada")).thenReturn(Optional.of(new EstadosFactura(12L, "Pagada", true)));
        when(facturaRepository.findByDescripcionIgnoreCase("Cancelada")).thenReturn(Optional.empty());

        assertThat(catalogo.facturaPendiente()).isEqualTo(11L);
        assertThat(catalogo.facturaPagada()).isEqualTo(12L);
        assertThrows(ReglaNegocioException.class, () -> catalogo.facturaCancelada());
    }
}
