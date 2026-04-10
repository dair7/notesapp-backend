package com.notesapp.controller;

import com.notesapp.dto.auth.AuthResponseDTO;
import com.notesapp.dto.auth.LoginRequestDTO;
import com.notesapp.dto.auth.RegisterRequestDTO;
import com.notesapp.dto.auth.ResetPasswordRequestDTO;

import com.notesapp.dto.auth.RefreshTokenRequestDTO;
import com.notesapp.service.interfaz.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ── POST /api/auth/register ──────────────────────────
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.ok(authService.registrar(dto));
    }

    // ── POST /api/auth/login ─────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.iniciarSesion(dto));
    }

    // ── POST /api/auth/refresh-token ─────────────────────
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDTO> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO dto) {
        return ResponseEntity.ok(authService.renovarToken(dto.getRefreshToken()));
    }

    // ── POST /api/auth/logout ────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Valid @RequestBody RefreshTokenRequestDTO dto) {
        authService.cerrarSesion(dto.getRefreshToken());
        return ResponseEntity.ok("Sesión cerrada exitosamente");
    }

    // ── POST /api/auth/forgot-password ───────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        authService.solicitarRecuperacionPassword(email);
        return ResponseEntity.ok("Si el correo está registrado, recibirás un código de recuperación.");
    }

    // ── POST /api/auth/reset-password ────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO dto) {
        authService.restablecerPassword(dto.getToken(), dto.getNewPassword());
        return ResponseEntity.ok("Contraseña actualizada exitosamente.");
    }
}
