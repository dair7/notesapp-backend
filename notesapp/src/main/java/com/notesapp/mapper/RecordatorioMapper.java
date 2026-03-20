package com.notesapp.mapper;

import com.notesapp.dto.recordatorioDTO.RecordatorioRequestDTO;
import com.notesapp.dto.recordatorioDTO.RecordatorioResponseDTO;
import com.notesapp.entity.Nota;
import com.notesapp.entity.Recordatorio;

public class RecordatorioMapper {

    public static Recordatorio toEntity(RecordatorioRequestDTO dto, Nota nota) {

        Recordatorio recordatorio = new Recordatorio();
        recordatorio.setFecha(dto.getFecha());
        recordatorio.setNota(nota);

        return recordatorio;
    }

    public static RecordatorioResponseDTO toResponseDTO(Recordatorio recordatorio) {

        RecordatorioResponseDTO dto = new RecordatorioResponseDTO();
        dto.setId(recordatorio.getId());
        dto.setFecha(recordatorio.getFecha());
        dto.setCompletado(recordatorio.getCompletado());

        if (recordatorio.getNota() != null) {
            dto.setNotaId(recordatorio.getNota().getId());
            dto.setNotaTitulo(recordatorio.getNota().getTitulo());
        }

        return dto;
    }
}