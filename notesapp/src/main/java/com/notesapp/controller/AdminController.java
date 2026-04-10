package com.notesapp.controller;

import com.notesapp.dto.adminDTO.PageResponseDTO;
import com.notesapp.dto.adminDTO.StatsMensualResponseDTO;
import com.notesapp.dto.adminDTO.StatsResponseDTO;
import com.notesapp.dto.adminDTO.ResetPasswordAdminDTO;
import com.notesapp.dto.notaDTO.NotaResponseDTO;
import com.notesapp.dto.usuarioDTO.UsuarioRequestDTO;
import com.notesapp.dto.usuarioDTO.UsuarioResponseDTO;
import com.notesapp.enums.EstadoUsuario;
import com.notesapp.enums.RoleType;
import com.notesapp.service.interfaz.AdminService;
import com.notesapp.service.interfaz.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UsuarioService usuarioService;
    private final AdminService adminService;

    public AdminController(UsuarioService usuarioService, AdminService adminService) {
        this.usuarioService = usuarioService;
        this.adminService = adminService;
    }

    // ── Estadísticas globales del sistema ───────────────
    @GetMapping("/stats")
    public ResponseEntity<StatsResponseDTO> obtenerEstadisticas() {
        return ResponseEntity.ok(adminService.obtenerEstadisticas());
    }

    // ── Estadísticas mensuales para gráficas (últimos 6 meses) ──
    @GetMapping("/stats/mensual")
    public ResponseEntity<StatsMensualResponseDTO> obtenerEstadisticasMensuales() {
        return ResponseEntity.ok(adminService.obtenerEstadisticasMensuales());
    }

    // ── Listar usuarios paginados con cantidad de notas ──
    @GetMapping("/usuarios")
    public ResponseEntity<PageResponseDTO<UsuarioResponseDTO>> listarUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listarUsuariosConDetalles(page, size));
    }

    // ── Ver un usuario por ID (ADMIN y SUPER_ADMIN) ──────
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    // ── Crear un nuevo administrador (ADMIN) ─────────────
    @PostMapping("/usuarios/admin")
    public ResponseEntity<UsuarioResponseDTO> crearAdmin(
            @Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.crearAdmin(dto));
    }

    // ── Crear un usuario normal con envío de credenciales por correo ──
    @PostMapping("/usuarios/usuario")
    public ResponseEntity<UsuarioResponseDTO> crearUsuario(
            @Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(adminService.crearUsuario(dto));
    }

    // ── Cambiar rol de un usuario (ADMIN) ─────────────────
    @PatchMapping("/usuarios/{id}/rol")
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

    // ── Editar nombre y email (ADMIN y SUPER_ADMIN) ──────
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(adminService.actualizarUsuario(id, dto));
    }

    // ── Resetear contraseña sin verificar la actual ──────
    @PutMapping("/usuarios/{id}/password")
    public ResponseEntity<String> resetearPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordAdminDTO dto) {
        adminService.resetearPassword(id, dto.getNuevaPassword());
        return ResponseEntity.ok("Contraseña actualizada exitosamente");
    }

    // ── Activar o desactivar cuenta (baja lógica) ────────
    @PatchMapping("/usuarios/{id}/estado")
    public ResponseEntity<UsuarioResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoUsuario estado) {
        return ResponseEntity.ok(adminService.cambiarEstado(id, estado));
    }

    // ── Ver todas las notas de un usuario ─────────────────
    @GetMapping("/usuarios/{id}/notas")
    public ResponseEntity<List<NotaResponseDTO>> obtenerNotasDeUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.obtenerNotasDeUsuario(id));
    }

    // ── Contar usuarios activos sin conectarse en los últimos N meses ──
    @GetMapping("/usuarios/inactivos-por-tiempo")
    public ResponseEntity<Long> contarInactivosPorTiempo(
            @RequestParam(defaultValue = "3") int meses) {
        return ResponseEntity.ok(adminService.contarInactivosPorTiempo(meses));
    }
}
