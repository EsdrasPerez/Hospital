package com.example.hospital.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.hospital.rol.Rol;
import com.example.hospital.rol.RolRepository;
import com.example.hospital.shared.security.UsuarioDetailsService;
import com.example.hospital.usuarios.Usuarios;
import com.example.hospital.usuarios.UsuariosRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock UsuariosRepository usuariosRepository;
    @Mock RolRepository rolRepository;
    @InjectMocks UsuarioDetailsService service;

    private Usuarios usuario(Boolean estado, Long idRol) {
        Usuarios u = new Usuarios();
        u.setNombreUsuario("medico1");
        u.setContrasenaHash("hash");
        u.setEstado(estado);
        u.setIdRol(idRol);
        return u;
    }

    @Test
    void loadUserByUsername_lanzaUsernameNotFound_siElUsuarioNoExiste() {
        when(usuariosRepository.findByNombreUsuario("nadie")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("nadie"));
    }

    @Test
    void loadUserByUsername_devuelveUsuarioDeshabilitado_siElUsuarioEstaInactivo() {
        when(usuariosRepository.findByNombreUsuario("medico1")).thenReturn(Optional.of(usuario(false, null)));

        UserDetails u = service.loadUserByUsername("medico1");

        assertThat(u.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_asignaElRolDeLaBaseDeDatosComoAutoridad() {
        Rol rol = new Rol();
        rol.setNombre("MEDICO");
        when(usuariosRepository.findByNombreUsuario("medico1")).thenReturn(Optional.of(usuario(true, 3L)));
        when(rolRepository.findById(3L)).thenReturn(Optional.of(rol));

        UserDetails u = service.loadUserByUsername("medico1");

        assertThat(u.isEnabled()).isTrue();
        assertThat(u.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_MEDICO");
    }

    @Test
    void loadUserByUsername_lanzaUsernameNotFound_siElRolAsignadoNoExiste() {
        when(usuariosRepository.findByNombreUsuario("medico1")).thenReturn(Optional.of(usuario(true, 99L)));
        when(rolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("medico1"));
    }
}
