package com.notesapp.dto.recordatorioDTO;

import com.notesapp.enums.Prioridad;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordatorioRequestDTO {

    @NotNull(message = "La fecha de la tarea es obligatoria")
    @Future(message = "La fecha de la tarea debe ser en el futuro")
    private LocalDateTime fecha;

    private Prioridad prioridad = Prioridad.MEDIA;
}
