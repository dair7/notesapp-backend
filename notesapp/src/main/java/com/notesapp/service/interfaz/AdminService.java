package com.notesapp.service.interfaz;

import com.notesapp.dto.adminDTO.StatsMensualResponseDTO;
import com.notesapp.dto.adminDTO.StatsResponseDTO;
import com.notesapp.dto.adminDTO.PageResponseDTO;
import com.notesapp.dto.notaDTO.NotaResponseDTO;
import com.notesapp.dto.usuarioDTO.UsuarioRequestDTO;
import com.notesapp.dto.usuarioDTO.UsuarioResponseDTO;
import com.notesapp.enums.EstadoUsuario;

import java.util.List;

public interface AdminService {

    StatsResponseDTO obtenerEstadisticas();

    // Estadísticas mensuales para las gráficas del dashboard
    StatsMensualResponseDTO obtenerEstadisticasMensuales();

    // Listar usuarios paginados incluyendo cantidad de notas de cada uno
    PageResponseDTO<UsuarioResponseDTO> listarUsuariosConDetalles(int page, int size);

    // Editar nombre y email de cualquier usuario
    UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO dto);

    // Resetear contraseña sin necesitar la contraseña actual
    void resetearPassword(Long id, String nuevaPassword);

    // Activar o desactivar una cuenta (baja lógica)
    UsuarioResponseDTO cambiarEstado(Long id, EstadoUsuario estado);

    // Obtener todas las notas de un usuario específico
    List<NotaResponseDTO> obtenerNotasDeUsuario(Long usuarioId);
}
