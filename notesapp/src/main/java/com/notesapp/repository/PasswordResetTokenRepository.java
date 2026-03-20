package com.notesapp.repository;

import com.notesapp.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUsuario(com.notesapp.entity.Usuario usuario);
    void deleteByUsuario(com.notesapp.entity.Usuario usuario);
}
