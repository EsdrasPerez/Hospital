package com.example.hospital.procedimientosemergencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.emergencia.Emergencia;
import com.example.hospital.emergencia.EmergenciaRepository;
import com.example.hospital.medico.Medico;
import com.example.hospital.medico.MedicoRepository;
import com.example.hospital.procedimientosemergencia.dto.ProcedimientosEmergenciaDTO;
import com.example.hospital.procedimientosemergencia.impl.ProcedimientosEmergenciaServiceImpl;
import com.example.hospital.shared.exception.ReglaNegocioException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcedimientosEmergenciaServiceImplTest {

    @Mock ProcedimientosEmergenciaRepository procedimientosRepository;
    @Mock EmergenciaRepository emergenciaRepository;
    @Mock MedicoRepository medicoRepository;
    @InjectMocks ProcedimientosEmergenciaServiceImpl service;

    private Emergencia emergencia(LocalDateTime alta) {
        Emergencia e = new Emergencia();
        e.setIdEmergencia(1L);
        e.setFechaHoraIngreso(LocalDateTime.now().minusHours(2));
        e.setFechaHoraAlta(alta);
        return e;
    }

    private Medico medico(boolean estado) {
        Medico m = new Medico();
        m.setEstado(estado);
        return m;
    }

    private ProcedimientosEmergenciaDTO dto() {
        return new ProcedimientosEmergenciaDTO(null, 1L, "  ECG  ", null, 5L, null, null);
    }

    @Test
    void crear_conMedicoInexistente_lanzaReglaNegocio422() {
        when(emergenciaRepository.findById(1L)).thenReturn(Optional.of(emergencia(null)));
        when(medicoRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto()));

        verify(procedimientosRepository, never()).save(any());
    }

    @Test
    void crear_conMedicoInactivo_lanzaReglaNegocio422() {
        when(emergenciaRepository.findById(1L)).thenReturn(Optional.of(emergencia(null)));
        when(medicoRepository.findById(5L)).thenReturn(Optional.of(medico(false)));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto()));
    }

    @Test
    void crear_conEmergenciaInexistente_lanzaReglaNegocio422() {
        when(emergenciaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto()));
    }

    @Test
    void crear_enEmergenciaCerrada_lanzaReglaNegocio422() {
        when(emergenciaRepository.findById(1L)).thenReturn(Optional.of(emergencia(LocalDateTime.now().minusMinutes(5))));

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto()));
    }

    @Test
    void crear_valido_guardaConDescripcionRecortadaYFechaActual() {
        when(emergenciaRepository.findById(1L)).thenReturn(Optional.of(emergencia(null)));
        when(medicoRepository.findById(5L)).thenReturn(Optional.of(medico(true)));
        when(procedimientosRepository.save(any(ProcedimientosEmergencia.class))).thenAnswer(inv -> inv.getArgument(0));

        var r = service.crear(dto());

        assertThat(r.descripcion()).isEqualTo("ECG");
        assertThat(r.fechaHora()).isNotNull();
        assertThat(r.activo()).isTrue();
    }
}
