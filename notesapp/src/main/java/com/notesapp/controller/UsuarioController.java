package com.notesapp.controller;

import com.notesapp.dto.usuarioDTO.UsuarioResponseDTO;
import com.notesapp.service.interfaz.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ── Ver mi perfil ────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPerfil(@PathVariable Long id) {
        usuarioService.verificarAccesoPropietario(id);
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    // ── Listar todos (Solo ADMIN) ────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
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
}
