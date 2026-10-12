package com.example.hospital.paciente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.alergias.AlergiasRepository;
import com.example.hospital.paciente.dto.PacienteRequest;
import com.example.hospital.paciente.dto.PacienteResponse;
import com.example.hospital.paciente.impl.PacienteServiceImpl;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.tiposangre.TipoSangreRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PacienteServiceImplTest {

    @Mock PacienteRepository repository;
    @Mock AlergiasRepository alergiasRepository;
    @Mock TipoSangreRepository tipoSangreRepository;
    @Spy PacienteMapper mapper = new PacienteMapper();
    @InjectMocks PacienteServiceImpl service;

    private PacienteRequest request(String dpi, Long idTipoSangre) {
        return new PacienteRequest(dpi, "Ana", "López", "Pérez", LocalDate.of(1990, 5, 10),
                "F", "55551234", "ana@mail.com", "Zona 1", null, idTipoSangre);
    }

    @Test
    void crear_asignaFechaRegistroYActivo_yGuarda() {
        when(repository.existsByDpiPaciente("2345678901234")).thenReturn(false);
        when(repository.save(any(Paciente.class))).thenAnswer(inv -> {
            Paciente p = inv.getArgument(0);
            p.setIdPaciente(1L);
            return p;
        });

        PacienteResponse r = service.crear(request("2345678901234", null));

        assertThat(r.idPaciente()).isEqualTo(1L);
        assertThat(r.activo()).isTrue();
        assertThat(r.fechaRegistro()).isNotNull();
    }

    @Test
    void crear_lanzaDuplicado_siElDpiYaExiste_yNoGuarda() {
        when(repository.existsByDpiPaciente("2345678901234")).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.crear(request("2345678901234", null)));

        verify(repository, never()).save(any());
    }

    @Test
    void crear_lanzaReglaNegocio_siElTipoDeSangreNoExiste() {
        when(repository.existsByDpiPaciente("2345678901234")).thenReturn(false);
        when(tipoSangreRepository.existsById(99L)).thenReturn(false);

        assertThrows(ReglaNegocioException.class, () -> service.crear(request("2345678901234", 99L)));

        verify(repository, never()).save(any());
    }

    @Test
    void obtener_lanzaNoEncontrado_siElPacienteNoExisteOEstaInactivo() {
        when(repository.findByIdPacienteAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(99L));
    }

    @Test
    void eliminar_haceBorradoLogico_noBorraLaFila() {
        Paciente p = new Paciente();
        p.setIdPaciente(1L);
        when(repository.findByIdPacienteAndActivoTrue(1L)).thenReturn(Optional.of(p));

        service.eliminar(1L);

        assertThat(p.getActivo()).isFalse();
        verify(repository).save(p);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void listar_devuelveSoloLosActivos() {
        Paciente p = new Paciente();
        p.setIdPaciente(1L);
        when(repository.findByActivoTrue()).thenReturn(List.of(p));

        assertThat(service.listar()).hasSize(1);
    }
}
