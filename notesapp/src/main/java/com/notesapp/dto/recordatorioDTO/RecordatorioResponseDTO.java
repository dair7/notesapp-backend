package com.notesapp.dto.recordatorioDTO;

import com.notesapp.entity.Prioridad;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordatorioResponseDTO {

    private Long id;
    private LocalDateTime fecha;
    private Boolean completado;
    private Long notaId;
    private String notaTitulo;
    private Prioridad prioridad;
}
