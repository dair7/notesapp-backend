package com.notesapp.service.impl;

import com.notesapp.dto.notaDTO.NotaRequestDTO;
import com.notesapp.dto.notaDTO.NotaResponseDTO;
import com.notesapp.entity.Nota;
import com.notesapp.entity.Usuario;
import com.notesapp.enums.EstadoNoteType;
import com.notesapp.exception.AccessDeniedException;
import com.notesapp.exception.ResourceNotFoundException;
import com.notesapp.mapper.NotaMapper;
import com.notesapp.repository.NotaRepository;
import com.notesapp.repository.UsuarioRepository;
import com.notesapp.repository.CategoriaRepository;
import com.notesapp.security.SecurityUtils;
import com.notesapp.service.interfaz.NotaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotaServiceImpl implements NotaService {

    private final NotaRepository notaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;

    public NotaServiceImpl(NotaRepository notaRepository,
                           UsuarioRepository usuarioRepository,
                           CategoriaRepository categoriaRepository) {
        this.notaRepository = notaRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public NotaResponseDTO crearNota(Long usuarioId, NotaRequestDTO dto) {
        // Verificar que el usuario crea nota para sí mismo (o es admin)
        verificarAcceso(usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));

        Nota nota = NotaMapper.toEntity(dto, usuario);
        
        if (dto.getCategoriaId() != null) {
            com.notesapp.entity.Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", dto.getCategoriaId()));
            nota.setCategoria(categoria);
        }
        
        Nota notaGuardada = notaRepository.save(nota);
        return NotaMapper.toResponseDTO(notaGuardada);
    }

    @Override
    public List<NotaResponseDTO> obtenerNotasPorUsuario(Long usuarioId) {
        verificarAcceso(usuarioId);

        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));

        return notaRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(NotaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotaResponseDTO> obtenerNotasPorEstado(Long usuarioId, EstadoNoteType estado) {
        verificarAcceso(usuarioId);

        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));

        return notaRepository.findByUsuarioIdAndEstado(usuarioId, estado)
                .stream()
                .map(NotaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotaResponseDTO> buscarNotas(Long usuarioId, String query) {
        verificarAcceso(usuarioId);

        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));

        return notaRepository.buscarNotas(usuarioId, query)
                .stream()
                .map(NotaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NotaResponseDTO obtenerNotaPorId(Long notaId) {
        Nota nota = notaRepository.findById(notaId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota", "id", notaId));

        // Verificar que la nota pertenece al usuario autenticado
        verificarPropietario(nota);

        return NotaMapper.toResponseDTO(nota);
    }

    @Override
    public NotaResponseDTO actualizarNota(Long notaId, NotaRequestDTO dto) {
        Nota nota = notaRepository.findById(notaId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota", "id", notaId));

        verificarPropietario(nota);

        nota.setTitulo(dto.getTitulo());
        nota.setContenido(dto.getContenido());
        nota.setColor(dto.getColor());
        nota.setEtiquetas(dto.getEtiquetas());
        
        if (dto.getCategoriaId() != null) {
            com.notesapp.entity.Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", dto.getCategoriaId()));
            nota.setCategoria(categoria);
        } else {
            nota.setCategoria(null);
        }

        Nota notaActualizada = notaRepository.save(nota);
        return NotaMapper.toResponseDTO(notaActualizada);
    }

    @Override
    public NotaResponseDTO cambiarEstado(Long notaId, EstadoNoteType estado) {
        Nota nota = notaRepository.findById(notaId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota", "id", notaId));

        verificarPropietario(nota);

        nota.setEstado(estado);
        Nota notaActualizada = notaRepository.save(nota);
        return NotaMapper.toResponseDTO(notaActualizada);
    }

    @Override
    public void eliminarNota(Long notaId) {
        Nota nota = notaRepository.findById(notaId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota", "id", notaId));

        verificarPropietario(nota);

        notaRepository.delete(nota);
    }

    // ── Anclar / Desanclar lote de notas ──
    @Override
    public void toggleAnclarNotas(List<Long> notasIds, boolean estadoAnclado) {
        List<Nota> notas = notaRepository.findAllById(notasIds);
        
        // Verificamos propiedad de cada una para evitar fallos de seguridad
        for (Nota nota : notas) {
            verificarPropietario(nota);
            nota.setEsAnclada(estadoAnclado);
        }
        
        notaRepository.saveAll(notas);
    }

    // ── Verificar que el usuario accede a sus propios datos ──
    private void verificarAcceso(Long usuarioId) {
        if (SecurityUtils.isAdmin()) return; // Admin ve todo

        String emailAuth = SecurityUtils.getAuthenticatedEmail();
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);

        if (usuario == null || !usuario.getEmail().equals(emailAuth)) {
            throw new AccessDeniedException("No tienes permiso para acceder a las notas de otro usuario");
        }
    }

    // ── Verificar que la nota pertenece al usuario autenticado ──
    private void verificarPropietario(Nota nota) {
        if (SecurityUtils.isAdmin()) return; // Admin ve todo

        String emailAuth = SecurityUtils.getAuthenticatedEmail();
        if (!nota.getUsuario().getEmail().equals(emailAuth)) {
            throw new AccessDeniedException("No tienes permiso para acceder a esta nota");
        }
    }
}