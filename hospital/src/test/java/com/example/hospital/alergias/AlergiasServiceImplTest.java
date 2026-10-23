package com.example.hospital.alergias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.alergias.dto.AlergiasDTO;
import com.example.hospital.alergias.impl.AlergiasServiceImpl;
import com.example.hospital.paciente.PacienteRepository;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlergiasServiceImplTest {

    @Mock AlergiasRepository repository;
    @Mock PacienteRepository pacienteRepository;
    @InjectMocks AlergiasServiceImpl service;

    @BeforeEach
    void preparar() {
        lenient().when(repository.save(any(Alergias.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Alergias existente() {
        Alergias a = new Alergias();
        a.setIdAlergias(1L);
        a.setNombre("Penicilina");
        a.setActivo(true);
        return a;
    }

    @Test
    void crear_sinActivo_naceActiva_yNoDa500PorNullEnLaColumna() {
        AlergiasDTO r = service.crear(new AlergiasDTO(null, "  Latex ", "x", null));

        assertThat(r.activo()).isTrue();
        assertThat(r.nombre()).isEqualTo("Latex");
    }

    @Test
    void crear_respetaActivoFalseSiSeEnvia() {
        assertThat(service.crear(new AlergiasDTO(null, "Polen", null, false)).activo()).isFalse();
    }

    @Test
    void crear_lanza409_siElNombreYaExiste_y422SiFalta() {
        when(repository.existsByNombreIgnoreCase("Latex")).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.crear(new AlergiasDTO(null, "Latex", null, null)));
        assertThrows(ReglaNegocioException.class, () -> service.crear(new AlergiasDTO(null, " ", null, null)));

        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_conActivoNulo_noMachacaElEstadoActual() {
        Alergias a = existente();
        when(repository.findById(1L)).thenReturn(Optional.of(a));

        service.actualizar(new AlergiasDTO(1L, "Penicilina G", "d", null), 1L);

        assertThat(a.getActivo()).isTrue();
        assertThat(a.getNombre()).isEqualTo("Penicilina G");
    }

    @Test
    void actualizar_lanza409_alRepetirElNombreDeOtra_y404SiNoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(existente()));
        when(repository.findById(99L)).thenReturn(Optional.empty());
        when(repository.existsByNombreIgnoreCaseAndIdAlergiasNot("Latex", 1L)).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.actualizar(new AlergiasDTO(1L, "Latex", null, null), 1L));
        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizar(new AlergiasDTO(99L, "X", null, null), 99L));
    }

    @Test
    void eliminar_lanza409_siHayPacientesConLaAlergia() {
        when(repository.findById(1L)).thenReturn(Optional.of(existente()));
        when(pacienteRepository.existsByIdAlergias(1L)).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.eliminar(1L));
    }
}
