package com.notesapp.mapper;

import com.notesapp.dto.notaDTO.NotaRequestDTO;
import com.notesapp.dto.notaDTO.NotaResponseDTO;
import com.notesapp.entity.Nota;
import com.notesapp.entity.Usuario;

public class NotaMapper {

    public static Nota toEntity(NotaRequestDTO dto, Usuario usuario) {

        Nota nota = new Nota();
        nota.setTitulo(dto.getTitulo());
        nota.setContenido(dto.getContenido());
        nota.setUsuario(usuario);

        return nota;
    }

    public static NotaResponseDTO toResponseDTO(Nota nota) {

        NotaResponseDTO dto = new NotaResponseDTO();
        dto.setId(nota.getId());
        dto.setTitulo(nota.getTitulo());
        dto.setContenido(nota.getContenido());
        dto.setFechaCreacion(nota.getFechaCreacion());
        dto.setEstado(nota.getEstado());
        dto.setEsAnclada(nota.isEsAnclada());

        if (nota.getUsuario() != null) {
            dto.setUsuarioId(nota.getUsuario().getId());
            dto.setUsuarioNombre(nota.getUsuario().getNombre());
        }

        return dto;
    }
}