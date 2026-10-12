package com.example.hospital.shared.security;

import com.example.hospital.rol.Rol;
import com.example.hospital.rol.RolRepository;
import com.example.hospital.usuarios.Usuarios;
import com.example.hospital.usuarios.UsuariosRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuariosRepository usuariosRepository;
    private final RolRepository rolRepository;

    public UsuarioDetailsService(UsuariosRepository usuariosRepository, RolRepository rolRepository) {
        this.usuariosRepository = usuariosRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuarios usuario = usuariosRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));

        String rolNombre = "USER";
        if (usuario.getIdRol() != null) {
            Rol rol = rolRepository.findById(usuario.getIdRol())
                    .orElseThrow(() -> new UsernameNotFoundException("Rol no asignado o inválido"));
            if (rol.getNombre() != null) {
                rolNombre = rol.getNombre();
            }
        }

        boolean enabled = usuario.getEstado() != null ? usuario.getEstado() : true;

        return User.withUsername(usuario.getNombreUsuario())
                .password(usuario.getContrasenaHash())
                .roles(rolNombre)
                .disabled(!enabled)
                .build();
    }
}
