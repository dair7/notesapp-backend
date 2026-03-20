package com.notesapp.service.impl;

import com.notesapp.dto.usuarioDTO.UsuarioRequestDTO;
import com.notesapp.dto.usuarioDTO.UsuarioResponseDTO;
import com.notesapp.entity.Usuario;
import com.notesapp.enums.RoleType;
import com.notesapp.exception.EmailAlreadyExistsException;
import com.notesapp.exception.ResourceNotFoundException;
import com.notesapp.mapper.UsuarioMapper;
import com.notesapp.repository.*;
import com.notesapp.service.interfaz.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NotaRepository notaRepository;
    private final RecordatorioRepository recordatorioRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder,
                              RefreshTokenRepository refreshTokenRepository,
                              NotaRepository notaRepository,
                              RecordatorioRepository recordatorioRepository,
                              VerificationTokenRepository verificationTokenRepository,
                              PasswordResetTokenRepository passwordResetTokenRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.notaRepository = notaRepository;
        this.recordatorioRepository = recordatorioRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    // ── Registro público (siempre rol USER) ──────────────
    @Override
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO dto) {

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException(dto.getEmail());
        }

        Usuario usuario = UsuarioMapper.toEntity(dto);
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRole(RoleType.USER); // Forzar rol USER

        Usuario guardado = usuarioRepository.save(usuario);
        return UsuarioMapper.toResponseDTO(guardado);
    }

    // ── Crear admin (solo desde AdminController) ─────────
    @Override
    public UsuarioResponseDTO crearAdmin(UsuarioRequestDTO dto) {

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException(dto.getEmail());
        }

        Usuario admin = UsuarioMapper.toEntity(dto);
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        admin.setRole(RoleType.ADMIN);   // Forzar rol ADMIN
        admin.setVerified(true);         // Admins no necesitan verificar email

        Usuario guardado = usuarioRepository.save(admin);
        return UsuarioMapper.toResponseDTO(guardado);
    }

    @Override
    public List<UsuarioResponseDTO> obtenerTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return UsuarioMapper.toResponseDTO(usuario);
    }

    @Override
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO dto) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (!usuario.getEmail().equals(dto.getEmail())
                && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException(dto.getEmail());
        }

        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return UsuarioMapper.toResponseDTO(actualizado);
    }

    // ── Cambiar rol (solo admin puede llamar esto) ───────
    @Override
    public UsuarioResponseDTO cambiarRol(Long id, RoleType nuevoRol) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        usuario.setRole(nuevoRol);

        Usuario actualizado = usuarioRepository.save(usuario);
        return UsuarioMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        // 1. Recordatorios (dependen de Nota)
        recordatorioRepository.deleteByNotaUsuarioId(id);
        // 2. Notas (dependen de Usuario)
        notaRepository.deleteByUsuarioId(id);
        // 3. Tokens (dependen de Usuario)
        refreshTokenRepository.deleteByUsuario(usuario);
        verificationTokenRepository.deleteByUsuario(usuario);
        passwordResetTokenRepository.deleteByUsuario(usuario);
        
        // 4. Usuario
        usuarioRepository.delete(usuario);
    }
}