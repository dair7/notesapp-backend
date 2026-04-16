package com.notesapp.dto.recordatorioDTO;

import com.notesapp.enums.Prioridad;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordatorioRequestDTO {

    // OffsetDateTime acepta strings con zona horaria (ej. "...Z" UTC) que envía Flutter
    @NotNull(message = "La fecha de la tarea es obligatoria")
    private OffsetDateTime fecha;

    private Prioridad prioridad = Prioridad.MEDIA;
}
