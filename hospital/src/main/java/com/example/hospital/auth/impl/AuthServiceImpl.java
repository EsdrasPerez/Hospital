package com.example.hospital.auth.impl;

import com.example.hospital.auth.AuthService;
import com.example.hospital.auth.dto.LoginRequest;
import com.example.hospital.auth.dto.LoginResponse;
import com.example.hospital.shared.security.JwtService;
import com.example.hospital.usuarios.Usuarios;
import com.example.hospital.usuarios.UsuariosRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           UsuariosRepository usuariosRepository,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuariosRepository = usuariosRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserDetails user = (UserDetails) auth.getPrincipal();
        String token = jwtService.generar(user);
        return new LoginResponse(token, "Bearer");
    }

    @Override
    public void register(LoginRequest request) {
        if (usuariosRepository.findByNombreUsuario(request.username()).isPresent()) {
            throw new IllegalArgumentException("El usuario ya existe");
        }

        Usuarios nuevoUsuario = new Usuarios();
        nuevoUsuario.setNombreUsuario(request.username());
        nuevoUsuario.setContrasenaHash(passwordEncoder.encode(request.password()));
        nuevoUsuario.setEstado(true);
        nuevoUsuario.setUltimoAcceso(LocalDateTime.now());
        nuevoUsuario.setIdRol(1L);

        usuariosRepository.save(nuevoUsuario);
    }
}
