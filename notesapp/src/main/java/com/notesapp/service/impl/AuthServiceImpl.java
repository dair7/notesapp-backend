package com.notesapp.service.impl;

import com.notesapp.dto.auth.AuthResponseDTO;
import com.notesapp.dto.auth.LoginRequestDTO;
import com.notesapp.dto.auth.RegisterRequestDTO;
import com.notesapp.entity.PasswordResetToken;
import com.notesapp.entity.RefreshToken;
import com.notesapp.entity.Usuario;
import com.notesapp.entity.VerificationToken;
import com.notesapp.enums.EstadoUsuario;
import com.notesapp.exception.BadRequestException;
import com.notesapp.exception.EmailAlreadyExistsException;
import com.notesapp.mapper.AuthMapper;
import com.notesapp.repository.PasswordResetTokenRepository;
import com.notesapp.repository.RefreshTokenRepository;
import com.notesapp.repository.UsuarioRepository;
import com.notesapp.repository.VerificationTokenRepository;
import com.notesapp.security.jwt.JwtTokenProvider;
import com.notesapp.service.interfaz.AuthService;
import com.notesapp.service.interfaz.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           VerificationTokenRepository verificationTokenRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailService = emailService;
    }

    // ── Registro ─────────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponseDTO registrar(RegisterRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException(dto.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));

        Usuario guardado = usuarioRepository.save(usuario);

        // Si requiere verificación de email, no se emite access token todavía
        if (!guardado.isVerified()) {
            String tokenVerificacion = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken(tokenVerificacion, guardado);
            verificationTokenRepository.save(verificationToken);
            // Fallo de SMTP no debe revertir el registro del usuario
            try {
                emailService.sendVerificationEmail(guardado.getEmail(), tokenVerificacion);
            } catch (Exception e) {
                log.error("Error al enviar email de verificación para {}: {}", guardado.getEmail(), e.getMessage(), e);
            }
            return AuthMapper.toResponseSinToken(guardado);
        }

        String token = jwtTokenProvider.generateToken(guardado.getEmail());
        return AuthMapper.toResponse(token, guardado);
    }

    // ── Login ────────────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponseDTO iniciarSesion(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        if (!usuario.isVerified()) {
            throw new BadRequestException("Debes verificar tu correo electrónico antes de iniciar sesión.");
        }

        if (usuario.getEstadoUsuario() == EstadoUsuario.INACTIVO) {
            throw new BadRequestException("Tu cuenta está inactiva. Contacta al administrador.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

            // Registrar fecha y hora del último login
            usuario.setUltimaConexion(LocalDateTime.now());
            usuarioRepository.save(usuario);

            String accessToken = jwtTokenProvider.generateToken(authentication);

            // Generar refresh token y guardarlo en DB
            String refreshTokenStr = UUID.randomUUID().toString();
            RefreshToken refreshToken = new RefreshToken(refreshTokenStr, usuario, refreshTokenExpiration);
            refreshTokenRepository.save(refreshToken);

            return AuthMapper.toResponse(accessToken, refreshTokenStr, usuario);

        } catch (BadCredentialsException e) {
            throw new BadRequestException("Email o contraseña incorrectos");
        }
    }

    // ── Renovar token ────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponseDTO renovarToken(String refreshTokenStr) {
        if (refreshTokenStr == null || refreshTokenStr.isBlank()) {
            throw new BadRequestException("El refresh token es obligatorio");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new BadRequestException("Refresh token inválido"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadRequestException("El refresh token ha expirado. Por favor inicia sesión nuevamente.");
        }

        Usuario usuario = refreshToken.getUsuario();

        // Rotación: eliminar el viejo y generar uno nuevo
        refreshTokenRepository.delete(refreshToken);
        String nuevoRefreshTokenStr = UUID.randomUUID().toString();
        RefreshToken nuevoRefreshToken = new RefreshToken(nuevoRefreshTokenStr, usuario, refreshTokenExpiration);
        refreshTokenRepository.save(nuevoRefreshToken);

        String nuevoAccessToken = jwtTokenProvider.generateToken(usuario.getEmail(), usuario.getRole().name());
        return AuthMapper.toResponse(nuevoAccessToken, nuevoRefreshTokenStr, usuario);
    }

    // ── Logout ───────────────────────────────────────────────
    @Override
    @Transactional
    public void cerrarSesion(String refreshTokenStr) {
        if (refreshTokenStr != null && !refreshTokenStr.isBlank()) {
            refreshTokenRepository.findByToken(refreshTokenStr)
                    .ifPresent(refreshTokenRepository::delete);
        }
    }

    // ── Recuperación de contraseña ───────────────────────────
    @Override
    public void solicitarRecuperacionPassword(String email) {
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);
        // Retorno silencioso: no revelar si el email existe o no
        if (optUsuario.isEmpty()) {
            return;
        }

        Usuario usuario = optUsuario.get();

        // Eliminar token previo si existía
        passwordResetTokenRepository.findByUsuario(usuario)
                .ifPresent(passwordResetTokenRepository::delete);

        String resetTokenStr = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        PasswordResetToken resetToken = new PasswordResetToken(resetTokenStr, usuario);
        passwordResetTokenRepository.save(resetToken);

        try {
            emailService.sendPasswordResetEmail(usuario.getEmail(), resetTokenStr);
        } catch (Exception e) {
            log.error("Error al enviar email de recuperación para {}: {}", usuario.getEmail(), e.getMessage(), e);
        }
    }

    // ── Restablecer contraseña ───────────────────────────────
    @Override
    @Transactional
    public void restablecerPassword(String token, String nuevaPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Código inválido"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El código ha expirado");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        passwordResetTokenRepository.delete(resetToken);
    }

    // ── Verificación de email ────────────────────────────────
    @Override
    @Transactional
    public void verificarEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException(
                        "El enlace de verificación es inválido o el código ya fue usado."));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(
                    "El enlace de verificación ha expirado. Por favor solicita uno nuevo.");
        }

        Usuario usuario = verificationToken.getUsuario();
        usuario.setVerified(true);
        usuarioRepository.save(usuario);

        // Borrar el token una vez usado
        verificationTokenRepository.delete(verificationToken);

        // Enviar correo de bienvenida (no crítico, fallo silencioso)
        try {
            emailService.sendWelcomeEmail(usuario.getEmail(), usuario.getNombre());
        } catch (Exception ignored) {}
    }
}
