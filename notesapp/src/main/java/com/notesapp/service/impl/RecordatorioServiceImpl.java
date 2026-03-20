package com.notesapp.service.impl;

import com.notesapp.dto.recordatorioDTO.RecordatorioRequestDTO;
import com.notesapp.dto.recordatorioDTO.RecordatorioResponseDTO;
import com.notesapp.entity.Nota;
import com.notesapp.entity.Recordatorio;
import com.notesapp.exception.AccessDeniedException;
import com.notesapp.exception.ResourceNotFoundException;
import com.notesapp.mapper.RecordatorioMapper;
import com.notesapp.repository.NotaRepository;
import com.notesapp.repository.RecordatorioRepository;
import com.notesapp.repository.UsuarioRepository;
import com.notesapp.security.SecurityUtils;
import com.notesapp.service.interfaz.RecordatorioService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecordatorioServiceImpl implements RecordatorioService {

    private final RecordatorioRepository recordatorioRepository;
    private final NotaRepository notaRepository;
    private final UsuarioRepository usuarioRepository;

    public RecordatorioServiceImpl(RecordatorioRepository recordatorioRepository,
                                   NotaRepository notaRepository,
                                   UsuarioRepository usuarioRepository) {
        this.recordatorioRepository = recordatorioRepository;
        this.notaRepository = notaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public RecordatorioResponseDTO crearRecordatorio(Long notaId, RecordatorioRequestDTO dto) {
        Nota nota = notaRepository.findById(notaId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota", "id", notaId));

        verificarPropietarioNota(nota);

        Recordatorio recordatorio = RecordatorioMapper.toEntity(dto, nota);
        Recordatorio guardado = recordatorioRepository.save(recordatorio);
        return RecordatorioMapper.toResponseDTO(guardado);
    }

    @Override
    public List<RecordatorioResponseDTO> obtenerPorNota(Long notaId) {
        Nota nota = notaRepository.findById(notaId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota", "id", notaId));

        verificarPropietarioNota(nota);

        return recordatorioRepository.findByNotaId(notaId)
                .stream()
                .map(RecordatorioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecordatorioResponseDTO> obtenerPorUsuario(Long usuarioId) {
        verificarAccesoUsuario(usuarioId);

        return recordatorioRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(RecordatorioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecordatorioResponseDTO> obtenerPendientesPorUsuario(Long usuarioId) {
        verificarAccesoUsuario(usuarioId);

        return recordatorioRepository.findPendientesByUsuarioId(usuarioId)
                .stream()
                .map(RecordatorioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RecordatorioResponseDTO marcarComoCompletado(Long recordatorioId) {
        Recordatorio recordatorio = recordatorioRepository.findById(recordatorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Recordatorio", "id", recordatorioId));

        verificarPropietarioNota(recordatorio.getNota());

        recordatorio.setCompletado(!recordatorio.getCompletado());
        Recordatorio actualizado = recordatorioRepository.save(recordatorio);
        return RecordatorioMapper.toResponseDTO(actualizado);
    }

    @Override
    public void eliminarRecordatorio(Long recordatorioId) {
        Recordatorio recordatorio = recordatorioRepository.findById(recordatorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Recordatorio", "id", recordatorioId));

        verificarPropietarioNota(recordatorio.getNota());

        recordatorioRepository.delete(recordatorio);
    }

    // ── Verificar que la nota pertenece al usuario autenticado ──
    private void verificarPropietarioNota(Nota nota) {
        if (SecurityUtils.isAdmin()) return;

        String emailAuth = SecurityUtils.getAuthenticatedEmail();
        if (!nota.getUsuario().getEmail().equals(emailAuth)) {
            throw new AccessDeniedException("No tienes permiso para acceder a los recordatorios de otro usuario");
        }
    }

    // ── Verificar acceso por ID de usuario ──
    private void verificarAccesoUsuario(Long usuarioId) {
        if (SecurityUtils.isAdmin()) return;

        String emailAuth = SecurityUtils.getAuthenticatedEmail();
        var usuario = usuarioRepository.findById(usuarioId).orElse(null);

        if (usuario == null || !usuario.getEmail().equals(emailAuth)) {
            throw new AccessDeniedException("No tienes permiso para acceder a los recordatorios de otro usuario");
        }
    }
}