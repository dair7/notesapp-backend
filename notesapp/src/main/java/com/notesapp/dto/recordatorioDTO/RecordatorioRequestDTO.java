package com.notesapp.dto.recordatorioDTO;

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

    @NotNull(message = "La fecha del recordatorio es obligatoria")
    @Future(message = "La fecha del recordatorio debe ser en el futuro")
    private LocalDateTime fecha;
}
