package com.example.hospital.tiposangre;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.paciente.PacienteRepository;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.tiposangre.dto.TipoSangreDTO;
import com.example.hospital.tiposangre.impl.TipoSangreServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Regla: `nombre` es el tipo (A+..O-, único, cabe en VARCHAR(10)); `descripcion` es texto libre opcional (precargados: "A positivo"). */
@ExtendWith(MockitoExtension.class)
class TipoSangreServiceImplTest {

    @Mock TipoSangreRepository repository;
    @Mock PacienteRepository pacienteRepository;
    @InjectMocks TipoSangreServiceImpl service;

    @BeforeEach
    void preparar() {
        lenient().when(repository.save(any(TipoSangre.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private TipoSangre precargado() {
        TipoSangre t = new TipoSangre();
        t.setIdTipoDeSangre(1L);
        t.setNombre("A+");
        t.setDescripcion("A positivo");
        t.setActivo(true);
        return t;
    }

    @Test
    void crear_normalizaElNombre_conservaLaDescripcionLibre_yNaceActivo() {
        TipoSangreDTO r = service.crear(new TipoSangreDTO(null, " ab- ", "AB negativo", null));

        assertThat(r.nombre()).isEqualTo("AB-");
        assertThat(r.descripcion()).isEqualTo("AB negativo");
        assertThat(r.activo()).isTrue();
        assertThat(r.nombre().length()).isLessThanOrEqualTo(10);
    }

    @Test
    void crear_sinDescripcion_noInventaUnValorPorDefectoQueNoCabe() {
        TipoSangreDTO r = service.crear(new TipoSangreDTO(null, "O+", null, null));

        assertThat(r.descripcion()).isNull();
        assertThat(r.nombre()).isEqualTo("O+");
    }

    @Test
    void crear_lanza422_conUnNombreFueraDeLaLista_oSinNombre() {
        assertThrows(ReglaNegocioException.class, () -> service.crear(new TipoSangreDTO(null, "Z+", null, null)));
        assertThrows(ReglaNegocioException.class, () -> service.crear(new TipoSangreDTO(null, " ", null, null)));

        verify(repository, never()).save(any());
    }

    @Test
    void crear_lanza409_siElNombreYaExiste_noUn500() {
        when(repository.existsByNombreIgnoreCase("A+")).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.crear(new TipoSangreDTO(null, "a+", null, null)));

        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_permiteEditarUnRegistroPrecargado_cuyaDescripcionNoEsUnTipo() {
        TipoSangre t = precargado();
        when(repository.findById(1L)).thenReturn(Optional.of(t));

        TipoSangreDTO r = service.actualizar(new TipoSangreDTO(1L, "A+", "Tipo A positivo (editado)", null), 1L);

        assertThat(r.descripcion()).isEqualTo("Tipo A positivo (editado)");
        assertThat(r.nombre()).isEqualTo("A+");
    }

    @Test
    void actualizar_lanza409_alUsarElNombreDeOtroRegistro() {
        when(repository.findById(1L)).thenReturn(Optional.of(precargado()));
        when(repository.existsByNombreIgnoreCaseAndIdTipoDeSangreNot("B+", 1L)).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.actualizar(new TipoSangreDTO(1L, "B+", null, null), 1L));
    }

    @Test
    void eliminar_lanza409_siHayPacientesConEseTipo() {
        when(repository.findById(1L)).thenReturn(Optional.of(precargado()));
        when(pacienteRepository.existsByIdTipoDeSangre(1L)).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.eliminar(1L));
    }
}
