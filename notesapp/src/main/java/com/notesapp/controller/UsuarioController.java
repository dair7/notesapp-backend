package com.notesapp.controller;

import com.notesapp.dto.usuarioDTO.ChangePasswordDTO;
import com.notesapp.dto.usuarioDTO.UsuarioRequestDTO;
import com.notesapp.dto.usuarioDTO.UsuarioResponseDTO;
import com.notesapp.entity.Usuario;
import com.notesapp.exception.AccessDeniedException;
import com.notesapp.exception.BadRequestException;
import com.notesapp.repository.*;
import com.notesapp.security.SecurityUtils;
import com.notesapp.service.interfaz.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NotaRepository notaRepository;
    private final RecordatorioRepository recordatorioRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public UsuarioController(UsuarioService usuarioService,
                             UsuarioRepository usuarioRepository,
                             PasswordEncoder passwordEncoder,
                             RefreshTokenRepository refreshTokenRepository,
                             NotaRepository notaRepository,
                             RecordatorioRepository recordatorioRepository,
                             VerificationTokenRepository verificationTokenRepository,
                             PasswordResetTokenRepository passwordResetTokenRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.notaRepository = notaRepository;
        this.recordatorioRepository = recordatorioRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    // ── Ver mi perfil ────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPerfil(@PathVariable Long id) {
        verificarPropietario(id);
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    // ── Listar todos (Solo ADMIN) ────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    // ── Actualizar mi perfil (nombre, email) ─────────────
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizarPerfil(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO dto) {
        verificarPropietario(id);
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, dto));
    }

    // ── Cambiar contraseña ───────────────────────────────
    @PutMapping("/{id}/password")
    public ResponseEntity<String> cambiarPassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordDTO dto) {
        verificarPropietario(id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        // Verificar contraseña actual
        if (!passwordEncoder.matches(dto.getCurrentPassword(), usuario.getPassword())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }

        usuario.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Contraseña actualizada exitosamente");
    }

    // ── Eliminar mi cuenta ───────────────────────────────
    @DeleteMapping("/me")
    public ResponseEntity<Void> eliminarMiCuenta() {
        String email = SecurityUtils.getAuthenticatedEmail();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        usuarioService.eliminarUsuario(usuario.getId());

        return ResponseEntity.noContent().build();
    }

    // ── Cambiar rol (Solo ADMIN) ─────────────────────────
    @PatchMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> cambiarRol(
            @PathVariable Long id,
            @RequestParam com.notesapp.enums.RoleType nuevoRol) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, nuevoRol));
    }

    // ── Eliminar usuario (Solo ADMIN) ────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    // ── Verificar que el usuario accede a su propio perfil ──
    private void verificarPropietario(Long id) {
        if (SecurityUtils.isAdmin()) return;

        String emailAuth = SecurityUtils.getAuthenticatedEmail();
        Usuario usuario = usuarioRepository.findById(id).orElse(null);

        if (usuario == null || !usuario.getEmail().equals(emailAuth)) {
            throw new AccessDeniedException("No tienes permiso para acceder al perfil de otro usuario");
        }
    }
}