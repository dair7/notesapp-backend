package com.notesapp.dto.auth;

import com.notesapp.dto.usuarioDTO.UsuarioResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {

    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UsuarioResponseDTO usuario;

    public AuthResponseDTO(String token, UsuarioResponseDTO usuario) {
        this.token = token;
        this.usuario = usuario;
    }

    public AuthResponseDTO(String token, String refreshToken, UsuarioResponseDTO usuario) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.usuario = usuario;
    }
}

