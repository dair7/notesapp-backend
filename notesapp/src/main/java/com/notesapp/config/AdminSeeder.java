package com.notesapp.config;

import com.notesapp.entity.Usuario;
import com.notesapp.enums.RoleType;
import com.notesapp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.nombre}")
    private String adminNombre;

    public AdminSeeder(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // Solo crea el admin si NO existe
        if (!usuarioRepository.existsByEmail(adminEmail)) {

            Usuario admin = new Usuario();
            admin.setNombre(adminNombre);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(RoleType.SUPER_ADMIN);
            admin.setVerified(true); // El admin principal siempre está verificado

            usuarioRepository.save(admin);

            System.out.println(" Admin creado: " + adminEmail);
        } else {
            // "Reparar" el admin si existe pero no está verificado
            usuarioRepository.findByEmail(adminEmail).ifPresent(admin -> {
                if (!admin.isVerified()) {
                    admin.setVerified(true);
                    usuarioRepository.save(admin);
                    System.out.println(" Admin existente marcado como verificado.");
                }
            });
            System.out.println("ℹ Admin ya existe: " + adminEmail);
        }
    }
}
