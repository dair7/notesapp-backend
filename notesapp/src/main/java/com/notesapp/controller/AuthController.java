package com.notesapp.controller;

import com.notesapp.dto.auth.AuthResponseDTO;
import com.notesapp.dto.auth.LoginRequestDTO;
import com.notesapp.dto.auth.RegisterRequestDTO;
import com.notesapp.dto.auth.GoogleAuthRequestDTO;
import com.notesapp.dto.usuarioDTO.UsuarioResponseDTO;
import com.notesapp.entity.RefreshToken;
import com.notesapp.entity.Usuario;
import com.notesapp.exception.BadRequestException;
import com.notesapp.exception.EmailAlreadyExistsException;
import com.notesapp.mapper.UsuarioMapper;
import com.notesapp.repository.RefreshTokenRepository;
import com.notesapp.repository.UsuarioRepository;
import com.notesapp.security.jwt.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import com.notesapp.entity.VerificationToken;
import com.notesapp.entity.PasswordResetToken;
import com.notesapp.repository.VerificationTokenRepository;
import com.notesapp.repository.PasswordResetTokenRepository;
import com.notesapp.service.interfaz.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

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

    @Value("${google.client.id}")
    private String googleClientId;

    public AuthController(AuthenticationManager authenticationManager,
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

    // ── POST /api/auth/google ────────────────────────────
    @PostMapping("/google")
    @Transactional
    public ResponseEntity<AuthResponseDTO> googleLogin(
            @Valid @RequestBody GoogleAuthRequestDTO dto) {
        
        try {
            NetHttpTransport transport = new NetHttpTransport();
            GsonFactory jsonFactory = new GsonFactory();
            
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(googleClientId))
                .build();
                
            GoogleIdToken idToken = verifier.verify(dto.getIdToken());
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                
                Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
                
                if (usuario == null) {
                    usuario = new Usuario();
                    usuario.setEmail(email);
                    usuario.setNombre(name != null ? name : "Usuario Google");
                    // Contraseña inútil pero necesaria para el campo NOT NULL
                    usuario.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); 
                    usuario.setRole(com.notesapp.enums.RoleType.USER);
                    usuario.setVerified(true);
                    usuario = usuarioRepository.save(usuario);
                } else {
                    if (!usuario.isVerified()) {
                        usuario.setVerified(true);
                        usuario = usuarioRepository.save(usuario);
                    }
                }
                
                String accessToken = jwtTokenProvider.generateToken(usuario.getEmail(), usuario.getRole().name());
                
                String refreshTokenStr = UUID.randomUUID().toString();
                RefreshToken refreshToken = new RefreshToken(refreshTokenStr, usuario, refreshTokenExpiration);
                refreshTokenRepository.save(refreshToken);
                
                UsuarioResponseDTO usuarioDTO = UsuarioMapper.toResponseDTO(usuario);
                
                return ResponseEntity.ok(new AuthResponseDTO(accessToken, refreshTokenStr, usuarioDTO));
                
            } else {
                throw new BadRequestException("Token de Google inválido");
            }
        } catch (Exception e) {
            throw new BadRequestException("Error validando el token de Google: " + e.getMessage());
        }
    }

    // ── POST /api/auth/register ──────────────────────────
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO dto) {

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException(dto.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));

        Usuario guardado = usuarioRepository.save(usuario);

        if (!guardado.isVerified()) {
            String tokenVerificacion = java.util.UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken(tokenVerificacion, guardado);
            verificationTokenRepository.save(verificationToken);

            emailService.sendVerificationEmail(guardado.getEmail(), tokenVerificacion);
            
            UsuarioResponseDTO usuarioDTO = UsuarioMapper.toResponseDTO(guardado);
            return ResponseEntity.ok(new AuthResponseDTO(null, usuarioDTO));
        }

        String token = jwtTokenProvider.generateToken(guardado.getEmail());
        UsuarioResponseDTO usuarioDTO = UsuarioMapper.toResponseDTO(guardado);
        return ResponseEntity.ok(new AuthResponseDTO(token, usuarioDTO));
    }

    // ── POST /api/auth/login ─────────────────────────────
    @PostMapping("/login")
    @Transactional
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {

        try {
            Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));
                    
            if (!usuario.isVerified()) {
                throw new BadRequestException("Debes verificar tu correo electrónico antes de iniciar sesión.");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getEmail(),
                            dto.getPassword()));

            // Generar access token JWT
            String accessToken = jwtTokenProvider.generateToken(authentication);
            
            // Generar refresh token (UUID aleatorio guardado en DB)
            String refreshTokenStr = java.util.UUID.randomUUID().toString();
            RefreshToken refreshToken = new RefreshToken(refreshTokenStr, usuario, refreshTokenExpiration);
            refreshTokenRepository.save(refreshToken);

            UsuarioResponseDTO usuarioDTO = UsuarioMapper.toResponseDTO(usuario);

            return ResponseEntity.ok(new AuthResponseDTO(accessToken, refreshTokenStr, usuarioDTO));

        } catch (BadCredentialsException e) {
            throw new BadRequestException("Email o contraseña incorrectos");
        }
    }

    // ── POST /api/auth/refresh-token ─────────────────────
    @PostMapping("/refresh-token")
    @Transactional
    public ResponseEntity<AuthResponseDTO> refreshToken(@RequestBody java.util.Map<String, String> body) {
        String refreshTokenStr = body.get("refreshToken");
        
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

        // Rotación: eliminar el refresh token viejo y crear uno nuevo
        refreshTokenRepository.delete(refreshToken);
        String nuevoRefreshTokenStr = java.util.UUID.randomUUID().toString();
        RefreshToken nuevoRefreshToken = new RefreshToken(nuevoRefreshTokenStr, usuario, refreshTokenExpiration);
        refreshTokenRepository.save(nuevoRefreshToken);

        // Generar nuevo access token
        String nuevoAccessToken = jwtTokenProvider.generateToken(usuario.getEmail(), usuario.getRole().name());

        UsuarioResponseDTO usuarioDTO = UsuarioMapper.toResponseDTO(usuario);

        return ResponseEntity.ok(new AuthResponseDTO(nuevoAccessToken, nuevoRefreshTokenStr, usuarioDTO));
    }

    // ── POST /api/auth/logout ────────────────────────────
    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<String> logout(@RequestBody java.util.Map<String, String> body) {
        String refreshTokenStr = body.get("refreshToken");
        
        if (refreshTokenStr != null && !refreshTokenStr.isBlank()) {
            refreshTokenRepository.findByToken(refreshTokenStr)
                    .ifPresent(refreshTokenRepository::delete);
        }
        
        return ResponseEntity.ok("Sesión cerrada exitosamente");
    }
    
    // ── POST /api/auth/forgot-password ───────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        String mensajeGenerico = "Si el correo está registrado, recibirás un código de recuperación.";
        
        java.util.Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);
        if (optUsuario.isEmpty()) {
            return ResponseEntity.ok(mensajeGenerico);
        }
        
        Usuario usuario = optUsuario.get();
                
        passwordResetTokenRepository.findByUsuario(usuario)
                .ifPresent(passwordResetTokenRepository::delete);
                
        String resetTokenStr = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        PasswordResetToken resetToken = new PasswordResetToken(resetTokenStr, usuario);
        passwordResetTokenRepository.save(resetToken);
        
        try {
            emailService.sendPasswordResetEmail(usuario.getEmail(), resetTokenStr);
        } catch (Exception e) {
            System.err.println("Error al enviar email de recuperación: " + e.getMessage());
        }
        
        return ResponseEntity.ok(mensajeGenerico);
    }
    
    // ── POST /api/auth/reset-password ────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody java.util.Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            throw new BadRequestException("El código y la nueva contraseña son obligatorios");
        }
        
        if (newPassword.length() < 6) {
            throw new BadRequestException("La contraseña debe tener al menos 6 caracteres");
        }
        
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Código inválido"));
                
        if (resetToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            throw new BadRequestException("El código ha expirado");
        }
        
        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
        
        passwordResetTokenRepository.delete(resetToken);
        
        return ResponseEntity.ok("Contraseña actualizada exitosamente.");
    }
}

