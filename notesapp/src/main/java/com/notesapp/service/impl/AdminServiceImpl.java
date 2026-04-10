package com.notesapp.service.impl;

import com.notesapp.dto.adminDTO.StatsMensualResponseDTO;
import com.notesapp.dto.adminDTO.StatsResponseDTO;
import com.notesapp.dto.notaDTO.NotaResponseDTO;
import com.notesapp.dto.usuarioDTO.UsuarioRequestDTO;
import com.notesapp.dto.usuarioDTO.UsuarioResponseDTO;
import com.notesapp.entity.Usuario;
import com.notesapp.enums.EstadoNoteType;
import com.notesapp.enums.EstadoUsuario;
import com.notesapp.enums.RoleType;
import com.notesapp.exception.EmailAlreadyExistsException;
import com.notesapp.exception.ResourceNotFoundException;
import com.notesapp.mapper.NotaMapper;
import com.notesapp.mapper.UsuarioMapper;
import com.notesapp.repository.NotaRepository;
import com.notesapp.repository.RecordatorioRepository;
import com.notesapp.repository.UsuarioRepository;
import com.notesapp.service.interfaz.AdminService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.notesapp.dto.adminDTO.PageResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final UsuarioRepository usuarioRepository;
    private final NotaRepository notaRepository;
    private final RecordatorioRepository recordatorioRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(UsuarioRepository usuarioRepository,
                            NotaRepository notaRepository,
                            RecordatorioRepository recordatorioRepository,
                            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.notaRepository = notaRepository;
        this.recordatorioRepository = recordatorioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public StatsResponseDTO obtenerEstadisticas() {
        // Estadísticas de usuarios
        long totalUsuarios       = usuarioRepository.count();
        long usuariosActivos     = usuarioRepository.countByEstadoUsuario(EstadoUsuario.ACTIVO);
        long usuariosInactivos   = usuarioRepository.countByEstadoUsuario(EstadoUsuario.INACTIVO);
        long usuariosVerificados = usuarioRepository.countByIsVerified(true);
        long totalAdmins         = usuarioRepository.countByRole(RoleType.ADMIN);
        long totalSuperAdmins    = usuarioRepository.countByRole(RoleType.SUPER_ADMIN);

        // Estadísticas de notas
        long notasActivas    = notaRepository.countByEstado(EstadoNoteType.ACTIVA);
        long notasArchivadas = notaRepository.countByEstado(EstadoNoteType.ARCHIVADA);
        long notasEliminadas = notaRepository.countByEstado(EstadoNoteType.ELIMINADA);
        long totalNotas      = notasActivas + notasArchivadas + notasEliminadas;

        // Estadísticas de recordatorios
        long recordatoriosPendientes  = recordatorioRepository.countByCompletado(false);
        long recordatoriosCompletados = recordatorioRepository.countByCompletado(true);
        long totalRecordatorios       = recordatoriosPendientes + recordatoriosCompletados;

        return new StatsResponseDTO(
                totalUsuarios,
                usuariosActivos,
                usuariosInactivos,
                usuariosVerificados,
                totalAdmins,
                totalSuperAdmins,
                totalNotas,
                notasActivas,
                notasArchivadas,
                notasEliminadas,
                totalRecordatorios,
                recordatoriosPendientes,
                recordatoriosCompletados
        );
    }

    @Override
    public StatsMensualResponseDTO obtenerEstadisticasMensuales() {
        // Primer día del mes de hace 6 meses
        LocalDateTime fechaInicio = LocalDateTime.now()
                .minusMonths(6)
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        List<Object[]> usuariosRaw = usuarioRepository.contarPorMes(fechaInicio);
        List<Object[]> notasRaw    = notaRepository.contarPorMes(fechaInicio);

        List<StatsMensualResponseDTO.DatoMensual> usuariosPorMes = usuariosRaw.stream()
                .map(row -> new StatsMensualResponseDTO.DatoMensual(
                        (String) row[0],
                        ((Number) row[1]).longValue()))
                .collect(Collectors.toList());

        List<StatsMensualResponseDTO.DatoMensual> notasPorMes = notasRaw.stream()
                .map(row -> new StatsMensualResponseDTO.DatoMensual(
                        (String) row[0],
                        ((Number) row[1]).longValue()))
                .collect(Collectors.toList());

        return new StatsMensualResponseDTO(usuariosPorMes, notasPorMes);
    }

    @Override
    public PageResponseDTO<UsuarioResponseDTO> listarUsuariosConDetalles(int page, int size) {
        // Ordenar por fecha de creación descendente (más recientes primero)
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Usuario> paginaUsuarios = usuarioRepository.findAll(pageable);

        List<UsuarioResponseDTO> contenido = paginaUsuarios.getContent().stream()
                .map(u -> {
                    UsuarioResponseDTO dto = UsuarioMapper.toResponseDTO(u);
                    dto.setCantidadNotas(notaRepository.countByUsuarioId(u.getId()));
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageResponseDTO<>(
                contenido,
                paginaUsuarios.getNumber(),
                paginaUsuarios.getTotalPages(),
                paginaUsuarios.getTotalElements()
        );
    }

    @Override
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        // Verificar que el email no esté en uso por otro usuario
        if (!usuario.getEmail().equals(dto.getEmail())
                && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException(dto.getEmail());
        }

        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());

        return UsuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    @Override
    public void resetearPassword(Long id, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }

    @Override
    public UsuarioResponseDTO cambiarEstado(Long id, EstadoUsuario estado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        usuario.setEstadoUsuario(estado);
        return UsuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    @Override
    public List<NotaResponseDTO> obtenerNotasDeUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario", "id", usuarioId);
        }
        return notaRepository.findByUsuarioId(usuarioId).stream()
                .map(NotaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
