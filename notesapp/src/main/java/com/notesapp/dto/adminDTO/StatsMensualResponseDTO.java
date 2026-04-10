package com.notesapp.dto.adminDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsMensualResponseDTO {

    // Nuevos usuarios registrados por mes (últimos 6 meses)
    private List<DatoMensual> usuariosPorMes;

    // Notas creadas por mes (últimos 6 meses)
    private List<DatoMensual> notasPorMes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatoMensual {
        private String mes;      // formato "YYYY-MM", ej: "2025-10"
        private long cantidad;
    }
}
