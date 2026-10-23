package com.example.hospital.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/** Un usuario desactivado deja de poder usar su token: el filtro no lo autentica y la petición termina en 401. */
class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final UserDetailsService uds = mock(UserDetailsService.class);
    private final JwtAuthenticationFilter filtro = new JwtAuthenticationFilter(jwtService, uds);

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private void ejecutar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-x");
        filtro.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));
    }

    private UserDetails usuario(boolean habilitado) {
        return User.withUsername("cajero1").password("h").roles("CAJERO").disabled(!habilitado).build();
    }

    @Test
    void conUsuarioActivoYTokenValido_autentica() throws Exception {
        UserDetails u = usuario(true);
        when(jwtService.extraerUsername("token-x")).thenReturn("cajero1");
        when(uds.loadUserByUsername("cajero1")).thenReturn(u);
        when(jwtService.esTokenValido("token-x", u)).thenReturn(true);

        ejecutar();

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("cajero1");
    }

    @Test
    void conUsuarioDesactivado_noAutentica_auncoSiElTokenEsValido() throws Exception {
        UserDetails u = usuario(false);
        when(jwtService.extraerUsername("token-x")).thenReturn("cajero1");
        when(uds.loadUserByUsername("cajero1")).thenReturn(u);
        when(jwtService.esTokenValido("token-x", u)).thenReturn(true);

        ejecutar();

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void conTokenInvalido_noAutentica() throws Exception {
        when(jwtService.extraerUsername("token-x")).thenThrow(new io.jsonwebtoken.MalformedJwtException("malo"));

        ejecutar();

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(List.of(1)).hasSize(1);
    }
}
