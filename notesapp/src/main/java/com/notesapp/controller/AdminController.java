package com.notesapp.controller;

import com.notesapp.dto.usuarioDTO.UsuarioRequestDTO;
import com.notesapp.dto.usuarioDTO.UsuarioResponseDTO;
import com.notesapp.enums.RoleType;
import com.notesapp.service.interfaz.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminController {

    private final UsuarioService usuarioService;

    public AdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ── Listar todos los usuarios (ADMIN y SUPER_ADMIN) ──
    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    // ── Ver un usuario por ID (ADMIN y SUPER_ADMIN) ──────
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    // ── Crear un nuevo administrador (SOLO SUPER_ADMIN) ──
    @PostMapping("/usuarios")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> crearAdmin(
            @Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.crearAdmin(dto));
    }

    // ── Cambiar rol de un usuario (SOLO SUPER_ADMIN) ─────
    @PatchMapping("/usuarios/{id}/rol")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> cambiarRol(
            @PathVariable Long id,
            @RequestParam RoleType rol) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, rol));
    }

    // ── Eliminar usuario (ADMIN y SUPER_ADMIN) ───────────
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
