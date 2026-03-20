package com.notesapp.service.interfaz;

import com.notesapp.dto.notaDTO.NotaRequestDTO;
import com.notesapp.dto.notaDTO.NotaResponseDTO;
import com.notesapp.enums.EstadoNoteType;

import java.util.List;

public interface NotaService {

    NotaResponseDTO crearNota(Long usuarioId, NotaRequestDTO dto);

    List<NotaResponseDTO> obtenerNotasPorUsuario(Long usuarioId);

    List<NotaResponseDTO> obtenerNotasPorEstado(Long usuarioId, EstadoNoteType estado);

    List<NotaResponseDTO> buscarNotas(Long usuarioId, String query);

    NotaResponseDTO obtenerNotaPorId(Long notaId);

    NotaResponseDTO actualizarNota(Long notaId, NotaRequestDTO dto);

    NotaResponseDTO cambiarEstado(Long notaId, EstadoNoteType estado);

    void toggleAnclarNotas(List<Long> notasIds, boolean estadoAnclado);

    void eliminarNota(Long notaId);
}