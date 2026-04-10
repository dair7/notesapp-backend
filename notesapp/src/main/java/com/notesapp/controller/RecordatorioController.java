package com.notesapp.controller;

import com.notesapp.dto.recordatorioDTO.RecordatorioRequestDTO;
import com.notesapp.dto.recordatorioDTO.RecordatorioResponseDTO;
import com.notesapp.service.interfaz.RecordatorioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recordatorios")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class RecordatorioController {

    private final RecordatorioService recordatorioService;

    public RecordatorioController(RecordatorioService recordatorioService) {
        this.recordatorioService = recordatorioService;
    }

    // Crear recordatorio asociado a una nota
    @PostMapping("/{notaId}")
    public ResponseEntity<RecordatorioResponseDTO> crearRecordatorio(
            @PathVariable Long notaId,
            @Valid @RequestBody RecordatorioRequestDTO dto) {
        return ResponseEntity.ok(recordatorioService.crearRecordatorio(notaId, dto));
    }

    // Obtener recordatorios de una nota
    @GetMapping("/nota/{notaId}")
    public ResponseEntity<List<RecordatorioResponseDTO>> obtenerPorNota(
            @PathVariable Long notaId) {
        return ResponseEntity.ok(recordatorioService.obtenerPorNota(notaId));
    }

    // Obtener todos los recordatorios de un usuario
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<RecordatorioResponseDTO>> obtenerPorUsuario(
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(recordatorioService.obtenerPorUsuario(usuarioId));
    }

    // Obtener solo los recordatorios pendientes de un usuario
    @GetMapping("/pendientes/usuario/{usuarioId}")
    public ResponseEntity<List<RecordatorioResponseDTO>> obtenerPendientes(
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(recordatorioService.obtenerPendientesPorUsuario(usuarioId));
    }

    // Actualizar fecha de un recordatorio
    @PutMapping("/{id}")
    public ResponseEntity<RecordatorioResponseDTO> actualizarFecha(
            @PathVariable Long id,
            @Valid @RequestBody RecordatorioRequestDTO dto) {
        return ResponseEntity.ok(recordatorioService.actualizarFecha(id, dto));
    }

    // Marcar como completado / desmarcar (toggle)
    @PutMapping("/{id}/completar")
    public ResponseEntity<RecordatorioResponseDTO> toggleCompletado(
            @PathVariable Long id) {
        return ResponseEntity.ok(recordatorioService.marcarComoCompletado(id));
    }

    // Eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRecordatorio(
            @PathVariable Long id) {
        recordatorioService.eliminarRecordatorio(id);
        return ResponseEntity.noContent().build();
    }
}