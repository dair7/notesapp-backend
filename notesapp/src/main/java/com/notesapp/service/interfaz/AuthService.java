package com.notesapp.service.interfaz;

import com.notesapp.dto.auth.AuthResponseDTO;
import com.notesapp.dto.auth.LoginRequestDTO;
import com.notesapp.dto.auth.RegisterRequestDTO;

public interface AuthService {

    /** Registra un nuevo usuario y envía el email de verificación. */
    AuthResponseDTO registrar(RegisterRequestDTO dto);

    /** Autentica al usuario y retorna access + refresh token. */
    AuthResponseDTO iniciarSesion(LoginRequestDTO dto);

    /** Rota el refresh token y emite un nuevo par de tokens. */
    AuthResponseDTO renovarToken(String refreshToken);

    /** Invalida el refresh token (logout). */
    void cerrarSesion(String refreshToken);

    /** Genera un token de recuperación y lo envía por email. */
    void solicitarRecuperacionPassword(String email);

    /** Valida el token de recuperación y actualiza la contraseña. */
    void restablecerPassword(String token, String nuevaPassword);

    /** Marca al usuario como verificado y envía el email de bienvenida. */
    void verificarEmail(String token);
}
