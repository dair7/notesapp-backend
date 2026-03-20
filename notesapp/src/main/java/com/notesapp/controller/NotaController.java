package com.notesapp.controller;

import com.notesapp.dto.notaDTO.NotaRequestDTO;
import com.notesapp.dto.notaDTO.NotaResponseDTO;
import com.notesapp.enums.EstadoNoteType;
import com.notesapp.service.interfaz.NotaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notas")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class NotaController {

    private final NotaService notaService;

    public NotaController(NotaService notaService) {
        this.notaService = notaService;
    }

    // Crear nota
    @PostMapping("/{usuarioId}")
    public ResponseEntity<NotaResponseDTO> crearNota(
            @PathVariable Long usuarioId,
            @Valid @RequestBody NotaRequestDTO dto) {
        return ResponseEntity.ok(notaService.crearNota(usuarioId, dto));
    }

    // Obtener notas por usuario
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<NotaResponseDTO>> obtenerNotasPorUsuario(
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(notaService.obtenerNotasPorUsuario(usuarioId));
    }

    // Filtrar notas por estado (ACTIVA, ARCHIVADA, ELIMINADA)
    @GetMapping("/usuario/{usuarioId}/estado/{estado}")
    public ResponseEntity<List<NotaResponseDTO>> obtenerNotasPorEstado(
            @PathVariable Long usuarioId,
            @PathVariable EstadoNoteType estado) {
        return ResponseEntity.ok(notaService.obtenerNotasPorEstado(usuarioId, estado));
    }

    // Buscar notas por título o contenido
    @GetMapping("/buscar")
    public ResponseEntity<List<NotaResponseDTO>> buscarNotas(
            @RequestParam Long usuarioId,
            @RequestParam String q) {
        return ResponseEntity.ok(notaService.buscarNotas(usuarioId, q));
    }

    // Obtener por ID
    @GetMapping("/{id}")
    public ResponseEntity<NotaResponseDTO> obtenerNotaPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(notaService.obtenerNotaPorId(id));
    }

    // Actualizar
    @PutMapping("/{id}")
    public ResponseEntity<NotaResponseDTO> actualizarNota(
            @PathVariable Long id,
            @Valid @RequestBody NotaRequestDTO dto) {
        return ResponseEntity.ok(notaService.actualizarNota(id, dto));
    }

    // Cambiar estado (ACTIVA, ARCHIVADA, ELIMINADA)
    @PutMapping("/{id}/estado")
    public ResponseEntity<NotaResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoNoteType estado) {
        return ResponseEntity.ok(notaService.cambiarEstado(id, estado));
    }

    // Eliminar nota permanentemente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNota(@PathVariable Long id) {
        notaService.eliminarNota(id);
        return ResponseEntity.noContent().build();
    }

    // Anclar o Desanclar masivamente
    @PutMapping("/anclar")
    public ResponseEntity<Void> anclarMultiplesNotas(
            @RequestBody List<Long> notasIds,
            @RequestParam boolean anclar) {
        notaService.toggleAnclarNotas(notasIds, anclar);
        return ResponseEntity.ok().build();
    }
}