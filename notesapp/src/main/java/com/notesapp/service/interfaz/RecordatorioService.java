package com.notesapp.service.interfaz;

import com.notesapp.dto.recordatorioDTO.RecordatorioRequestDTO;
import com.notesapp.dto.recordatorioDTO.RecordatorioResponseDTO;

import java.util.List;

public interface RecordatorioService {

    RecordatorioResponseDTO crearRecordatorio(Long notaId, RecordatorioRequestDTO dto);

    List<RecordatorioResponseDTO> obtenerPorNota(Long notaId);

    List<RecordatorioResponseDTO> obtenerPorUsuario(Long usuarioId);

    List<RecordatorioResponseDTO> obtenerPendientesPorUsuario(Long usuarioId);

    RecordatorioResponseDTO marcarComoCompletado(Long recordatorioId);

    RecordatorioResponseDTO actualizarFecha(Long recordatorioId, RecordatorioRequestDTO dto);

    void eliminarRecordatorio(Long recordatorioId);
}