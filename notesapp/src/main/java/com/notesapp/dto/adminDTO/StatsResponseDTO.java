package com.notesapp.dto.adminDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponseDTO {

    // Estadísticas de usuarios
    private long totalUsuarios;
    private long usuariosActivos;
    private long usuariosInactivos;
    private long usuariosVerificados;
    private long totalAdmins;      // ADMIN + SUPER_ADMIN
    private long totalSuperAdmins;

    // Estadísticas de notas
    private long totalNotas;
    private long notasActivas;
    private long notasArchivadas;
    private long notasEliminadas;

    // Estadísticas de recordatorios
    private long totalRecordatorios;
    private long recordatoriosPendientes;
    private long recordatoriosCompletados;
}
