package com.example.hospital.rol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.rol.dto.RolDTO;
import com.example.hospital.rol.impl.RolServiceImpl;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.usuarios.UsuariosRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RolServiceImplTest {

    @Mock RolRepository rolRepository;
    @Mock UsuariosRepository usuariosRepository;
    @InjectMocks RolServiceImpl service;

    @BeforeEach
    void preparar() {
        lenient().when(rolRepository.save(any(Rol.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Rol rol(Long id, String nombre) {
        Rol r = new Rol();
        r.setIdRol(id);
        r.setNombre(nombre);
        r.setActivo(true);
        return r;
    }

    @Test
    void actualizar_lanza422_alRenombrarUnRolBase() {
        Rol cajero = rol(6L, "CAJERO");
        when(rolRepository.findById(6L)).thenReturn(Optional.of(cajero));

        assertThrows(ReglaNegocioException.class, () -> service.actualizar(new RolDTO(6L, "TESORERO", "x", true), 6L));

        assertThat(cajero.getNombre()).isEqualTo("CAJERO");
        verify(rolRepository, never()).save(any());
    }

    @Test
    void actualizar_permiteCambiarLaDescripcionDeUnRolBase_conElMismoNombre() {
        Rol medico = rol(2L, "MEDICO");
        when(rolRepository.findById(2L)).thenReturn(Optional.of(medico));

        RolDTO r = service.actualizar(new RolDTO(2L, "medico", "Personal médico", null), 2L);

        assertThat(r.nombre()).isEqualTo("MEDICO");
        assertThat(r.descripcion()).isEqualTo("Personal médico");
    }

    @Test
    void actualizar_permiteRenombrarUnRolNoBase() {
        Rol extra = rol(9L, "AUDITOR");
        when(rolRepository.findById(9L)).thenReturn(Optional.of(extra));

        assertThat(service.actualizar(new RolDTO(9L, "AUDITOR2", null, true), 9L).nombre()).isEqualTo("AUDITOR2");
    }

    @Test
    void eliminar_lanza409_siHayUsuariosActivosConEseRol() {
        when(rolRepository.findById(6L)).thenReturn(Optional.of(rol(6L, "CAJERO")));
        when(usuariosRepository.existsByIdRolAndEstadoTrue(6L)).thenReturn(true);

        assertThrows(DuplicadoException.class, () -> service.eliminar(6L));

        verify(rolRepository, never()).save(any());
    }

    @Test
    void eliminar_desactivaElRol_siNoHayUsuariosActivos() {
        Rol r = rol(9L, "AUDITOR");
        when(rolRepository.findById(9L)).thenReturn(Optional.of(r));
        when(usuariosRepository.existsByIdRolAndEstadoTrue(9L)).thenReturn(false);

        service.eliminar(9L);

        assertThat(r.getActivo()).isFalse();
    }
}
