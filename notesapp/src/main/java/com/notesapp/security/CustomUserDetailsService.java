package com.notesapp.security;

import com.notesapp.entity.Usuario;
import com.notesapp.enums.EstadoUsuario;
import com.notesapp.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con email: " + email));

        // isEnabled refleja si la cuenta está ACTIVA o INACTIVA
        // El filtro JWT revisa este flag antes de autenticar cada request
        boolean enabled = usuario.getEstadoUsuario() == EstadoUsuario.ACTIVO;

        return new User(
                usuario.getEmail(),
                usuario.getPassword(),
                enabled,
                true,  // accountNonExpired
                true,  // credentialsNonExpired
                true,  // accountNonLocked
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name())));
    }
}
