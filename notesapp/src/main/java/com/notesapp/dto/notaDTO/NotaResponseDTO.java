package com.notesapp.dto.notaDTO;

import com.notesapp.enums.EstadoNoteType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotaResponseDTO {

    private Long id;
    private String titulo;
    private String contenido;
    private LocalDateTime fechaCreacion;
    private EstadoNoteType estado;
    private boolean esAnclada;
    private Long usuarioId;
    private String usuarioNombre;
}
