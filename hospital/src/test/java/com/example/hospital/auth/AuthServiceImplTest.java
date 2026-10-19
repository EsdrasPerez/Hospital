package com.example.hospital.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hospital.auth.dto.LoginRequest;
import com.example.hospital.auth.dto.LoginResponse;
import com.example.hospital.auth.dto.RegisterRequest;
import com.example.hospital.auth.impl.AuthServiceImpl;
import com.example.hospital.rol.Rol;
import com.example.hospital.rol.RolRepository;
import com.example.hospital.shared.exception.DuplicadoException;
import com.example.hospital.shared.exception.ReglaNegocioException;
import com.example.hospital.shared.security.JwtService;
import com.example.hospital.usuarios.Usuarios;
import com.example.hospital.usuarios.UsuariosRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @Mock UsuariosRepository usuariosRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock RolRepository rolRepository;
    @InjectMocks AuthServiceImpl service;

    @Test
    void login_conCredencialesValidas_devuelveElTokenBearerGeneradoParaElUsuario() {
        UserDetails user = User.withUsername("admin").password("hash").roles("ADMIN").build();
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);
        when(jwtService.generar(user)).thenReturn("token-jwt");

        LoginResponse r = service.login(new LoginRequest("admin", "secreta"));

        assertThat(r.token()).isEqualTo("token-jwt");
        assertThat(r.type()).isEqualTo("Bearer");
    }

    @Test
    void login_propagaBadCredentials_yNoGeneraToken_siLasCredencialesSonInvalidas() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Credenciales invalidas"));

        assertThrows(BadCredentialsException.class, () -> service.login(new LoginRequest("admin", "mala")));

        verify(jwtService, never()).generar(any());
    }

    @Test
    void login_enviaUsuarioYContrasenaAlAuthenticationManager() {
        UserDetails user = User.withUsername("admin").password("hash").roles("ADMIN").build();
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(jwtService.generar(user)).thenReturn("t");

        service.login(new LoginRequest("admin", "secreta"));

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("admin");
        assertThat(captor.getValue().getCredentials()).isEqualTo("secreta");
    }

    @Test
    void register_guardaElUsuarioActivo_conLaContrasenaCifrada_nuncaEnTextoPlano() {
        when(usuariosRepository.findByNombreUsuario("nuevo")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secreta")).thenReturn("hash-bcrypt");
        when(rolRepository.findByNombre("RECEPCIONISTA")).thenReturn(Optional.of(rol(4L, "RECEPCIONISTA")));

        service.register(new RegisterRequest("nuevo", "secreta", "RECEPCIONISTA"));

        ArgumentCaptor<Usuarios> captor = ArgumentCaptor.forClass(Usuarios.class);
        verify(usuariosRepository).save(captor.capture());
        assertThat(captor.getValue().getNombreUsuario()).isEqualTo("nuevo");
        assertThat(captor.getValue().getContrasenaHash()).isEqualTo("hash-bcrypt");
        assertThat(captor.getValue().getEstado()).isTrue();
        assertThat(captor.getValue().getIdRol()).isEqualTo(4L);
    }

    @Test
    void register_usaElRolEnviadoEnElRequest() {
        when(usuariosRepository.findByNombreUsuario("medico1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("x")).thenReturn("h");
        when(rolRepository.findByNombre("MEDICO")).thenReturn(Optional.of(rol(2L, "MEDICO")));

        service.register(new RegisterRequest("medico1", "x", "medico"));

        ArgumentCaptor<Usuarios> captor = ArgumentCaptor.forClass(Usuarios.class);
        verify(usuariosRepository).save(captor.capture());
        assertThat(captor.getValue().getIdRol()).isEqualTo(2L);
    }

    @Test
    void register_rechazaUnRolInexistente_yNoGuarda() {
        when(usuariosRepository.findByNombreUsuario("u")).thenReturn(Optional.empty());
        when(rolRepository.findByNombre("INVENTADO")).thenReturn(Optional.empty());

        assertThrows(ReglaNegocioException.class, () -> service.register(new RegisterRequest("u", "x", "INVENTADO")));

        verify(usuariosRepository, never()).save(any());
    }

    private static Rol rol(Long id, String nombre) {
        Rol r = new Rol();
        r.setIdRol(id);
        r.setNombre(nombre);
        r.setActivo(true);
        return r;
    }

    @Test
    void register_rechazaElUsuarioDuplicado_yNoGuarda() {
        when(usuariosRepository.findByNombreUsuario("admin")).thenReturn(Optional.of(new Usuarios()));

        assertThrows(DuplicadoException.class, () -> service.register(new RegisterRequest("admin", "x", "ADMIN")));

        verify(usuariosRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}
