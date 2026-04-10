package com.notesapp.mapper;

import com.notesapp.dto.usuarioDTO.UsuarioRequestDTO;
import com.notesapp.dto.usuarioDTO.UsuarioResponseDTO;
import com.notesapp.entity.Usuario;

public class UsuarioMapper {

    public static Usuario toEntity(UsuarioRequestDTO dto) {

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword()); // luego irá encriptado

        return usuario;
    }

    public static UsuarioResponseDTO toResponseDTO(Usuario usuario) {

        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setRole(usuario.getRole());
        dto.setEstadoUsuario(usuario.getEstadoUsuario());
        dto.setCreatedAt(usuario.getCreatedAt());
        dto.setUltimaConexion(usuario.getUltimaConexion());

        return dto;
    }
}