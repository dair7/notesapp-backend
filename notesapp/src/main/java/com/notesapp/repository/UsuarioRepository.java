package com.notesapp.repository;

import com.notesapp.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Creará la consulta automaticamente solo con el nombre del metodo
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
