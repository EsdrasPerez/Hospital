package com.example.hospital.usuarios.impl;

import com.example.hospital.usuarios.Usuarios;
import com.example.hospital.shared.exception.RecursoNoEncontradoException;
import com.example.hospital.usuarios.UsuariosRepository;
import com.example.hospital.usuarios.UsuariosService;
import com.example.hospital.usuarios.dto.UsuariosDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuariosServiceImpl implements UsuariosService {

    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuariosServiceImpl(UsuariosRepository usuariosRepository, PasswordEncoder passwordEncoder) {
        this.usuariosRepository = usuariosRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UsuariosDTO mapToDTO(Usuarios u) {
        return UsuariosDTO.sinPassword(
                u.getIdUsuario(),
                u.getNombreUsuario(),
                u.getEstado(),
                u.getUltimoAcceso(),
                u.getIdRol()
        );
    }

    @Override
    public List<UsuariosDTO> findAll() {
        return usuariosRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UsuariosDTO findById(Long id) {
        Usuarios u = usuariosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id: " + id));
        return mapToDTO(u);
    }

    @Override
    @Transactional
    public UsuariosDTO crear(UsuariosDTO dto) {
        if (dto == null || dto.nombreUsuario() == null || dto.nombreUsuario().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        if (dto.password() == null || dto.password().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (usuariosRepository.findByNombreUsuario(dto.nombreUsuario()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }

        Usuarios usuario = new Usuarios();
        usuario.setNombreUsuario(dto.nombreUsuario());
        // Siempre se hashea la contraseña
        usuario.setContrasenaHash(passwordEncoder.encode(dto.password()));
        usuario.setEstado(dto.estado() != null ? dto.estado() : true);
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuario.setIdRol(dto.idRol() != null ? dto.idRol() : 1L);

        Usuarios guardado = usuariosRepository.save(usuario);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public UsuariosDTO actualizar(UsuariosDTO dto, Long id) {
        if (dto == null || id == null) {
            throw new IllegalArgumentException("Datos para actualizar inválidos");
        }

        Usuarios usuarioExistente = usuariosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario a actualizar con id: " + id));

        // Permite editar el nombre de usuario
        if (dto.nombreUsuario() != null && !dto.nombreUsuario().isBlank()) {
            usuarioExistente.setNombreUsuario(dto.nombreUsuario());
        }

        // Regla de negocio: si se envía contraseña, siempre se hashea
        if (dto.password() != null && !dto.password().isBlank()) {
            usuarioExistente.setContrasenaHash(passwordEncoder.encode(dto.password()));
        }

        if (dto.estado() != null) {
            usuarioExistente.setEstado(dto.estado());
        }

        if (dto.idRol() != null) {
            usuarioExistente.setIdRol(dto.idRol());
        }

        Usuarios actualizado = usuariosRepository.save(usuarioExistente);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Usuarios usuario = usuariosRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario a eliminar con id: " + id));
        usuario.setEstado(false);
        usuariosRepository.save(usuario);
    }
}
