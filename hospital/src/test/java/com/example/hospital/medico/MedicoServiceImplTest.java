package com.example.hospital.medico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.citas.CitasRepository;
import com.example.hospital.emergencia.EmergenciaRepository;
import com.example.hospital.hospitalizaciones.HospitalizacionesRepository;
import com.example.hospital.medico.dto.MedicoDTO;
import com.example.hospital.medico.impl.MedicoServiceImpl;
import com.example.hospital.shared.exception.ReglaNegocioException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MedicoServiceImplTest {

    @Mock MedicoRepository medicoRepository;
    @Mock CitasRepository citasRepository;
    @Mock HospitalizacionesRepository hospitalizacionesRepository;
    @Mock EmergenciaRepository emergenciaRepository;
    @InjectMocks MedicoServiceImpl service;

    private MedicoDTO dto(String colegiado, String nombre, String email, String telefono) {
        return new MedicoDTO(null, colegiado, nombre, "Perez", null, telefono, email, null);
    }

    private Medico medico(Long id, boolean estado) {
        Medico m = new Medico();
        m.setIdMedico(id);
        m.setNoColegiado("12345");
        m.setNombre("Luis");
        m.setApellidoPaterno("Perez");
        m.setEstado(estado);
        return m;
    }

    @Test
    void crear_normalizaLosDatos_yLoDejaActivo() {
        when(medicoRepository.existsByNoColegiado("12345")).thenReturn(false);
        when(medicoRepository.existsByEmailIgnoreCase("luis@mail.com")).thenReturn(false);
        when(medicoRepository.save(any(Medico.class))).thenAnswer(inv -> {
            Medico m = inv.getArgument(0);
            m.setIdMedico(1L);
            return m;
        });

        MedicoDTO r = service.crear(dto(" 12345 ", " Luis ", " Luis@Mail.com ", "55551234"));

        assertThat(r.idMedico()).isEqualTo(1L);
        assertThat(r.noColegiado()).isEqualTo("12345");
        assertThat(r.nombre()).isEqualTo("Luis");
        assertThat(r.email()).isEqualTo("luis@mail.com");
        assertThat(r.estado()).isTrue();
    }

    @Test
    void crear_lanzaReglaNegocio_siElEmailTieneFormatoInvalido_yNoGuarda() {
        when(medicoRepository.existsByNoColegiado("12345")).thenReturn(false);

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto("12345", "Luis", "no-es-correo", null)));

        verify(medicoRepository, never()).save(any());
    }

    @Test
    void crear_lanzaReglaNegocio_siElNombreEstaVacio_yNoGuarda() {
        when(medicoRepository.existsByNoColegiado("12345")).thenReturn(false);

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto("12345", "  ", null, null)));

        verify(medicoRepository, never()).save(any());
    }

    @Test
    void crear_lanzaReglaNegocio_siElTelefonoEsMuyCorto_yNoGuarda() {
        when(medicoRepository.existsByNoColegiado("12345")).thenReturn(false);

        assertThrows(ReglaNegocioException.class, () -> service.crear(dto("12345", "Luis", null, "123")));

        verify(medicoRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_lanzaReglaNegocio_alDesactivarUnMedicoConCitasActivas_yNoGuarda() {
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico(1L, true)));
        when(citasRepository.existsByIdMedicoAndActivoTrue(1L)).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(1L, false));

        verify(medicoRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_lanzaReglaNegocio_alDesactivarUnMedicoConHospitalizacionActiva_yNoGuarda() {
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico(1L, true)));
        when(citasRepository.existsByIdMedicoAndActivoTrue(1L)).thenReturn(false);
        when(hospitalizacionesRepository.existsByIdMedicoResponsableAndIdEstadoHosp(1L, 1L)).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(1L, false));

        verify(medicoRepository, never()).save(any());
    }

    @Test
    void eliminar_haceBorradoLogico_siNoTieneCompromisosActivos() {
        Medico m = medico(1L, true);
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(m));
        when(citasRepository.existsByIdMedicoAndActivoTrue(1L)).thenReturn(false);
        when(hospitalizacionesRepository.existsByIdMedicoResponsableAndIdEstadoHosp(1L, 1L)).thenReturn(false);
        when(emergenciaRepository.findByIdMedicoAndActivoTrue(1L)).thenReturn(List.of());

        service.eliminar(1L);

        assertThat(m.getEstado()).isFalse();
        verify(medicoRepository).save(m);
        verify(medicoRepository, never()).deleteById(any());
    }

    @Test
    void eliminar_lanzaReglaNegocio_siElMedicoYaEstaInactivo_yNoGuarda() {
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico(1L, false)));

        assertThrows(ReglaNegocioException.class, () -> service.eliminar(1L));

        verify(medicoRepository, never()).save(any());
    }

    @Test
    void buscar_sinCriterio_devuelveSoloLosMedicosActivos() {
        when(medicoRepository.findByEstadoTrue()).thenReturn(List.of(medico(1L, true)));

        assertThat(service.buscar("  ")).hasSize(1);
        verify(medicoRepository, never())
                .findByNombreContainingIgnoreCaseOrApellidoPaternoContainingIgnoreCase(any(), any());
    }
}
