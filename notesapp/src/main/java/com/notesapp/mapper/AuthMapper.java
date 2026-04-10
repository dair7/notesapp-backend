package com.notesapp.mapper;

import com.notesapp.dto.auth.AuthResponseDTO;
import com.notesapp.entity.Usuario;

public class AuthMapper {

    /** Respuesta sin access token (usuario pendiente de verificar email). */
    public static AuthResponseDTO toResponseSinToken(Usuario usuario) {
        return new AuthResponseDTO(null, UsuarioMapper.toResponseDTO(usuario));
    }

    /** Respuesta con access token pero sin refresh (registro inmediato). */
    public static AuthResponseDTO toResponse(String accessToken, Usuario usuario) {
        return new AuthResponseDTO(accessToken, UsuarioMapper.toResponseDTO(usuario));
    }

    /** Respuesta completa con access token y refresh token (login / renovación). */
    public static AuthResponseDTO toResponse(String accessToken, String refreshToken, Usuario usuario) {
        return new AuthResponseDTO(accessToken, refreshToken, UsuarioMapper.toResponseDTO(usuario));
    }
}
