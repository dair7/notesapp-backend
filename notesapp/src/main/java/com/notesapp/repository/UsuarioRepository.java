package com.notesapp.repository;

import com.notesapp.entity.Usuario;
import com.notesapp.enums.EstadoUsuario;
import com.notesapp.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Creará la consulta automaticamente solo con el nombre del metodo
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    // Conteo por rol (para estadísticas del dashboard)
    long countByRole(RoleType role);

    // Conteo de usuarios verificados
    long countByIsVerified(boolean isVerified);

    // Conteo por estado de cuenta (para estadísticas del dashboard)
    long countByEstadoUsuario(EstadoUsuario estadoUsuario);

    // Usuarios registrados por mes — últimos N meses (para gráfica del dashboard)
    @Query(value = "SELECT TO_CHAR(created_at, 'YYYY-MM') AS mes, COUNT(*) AS cantidad " +
                   "FROM usuarios WHERE created_at >= :fechaInicio " +
                   "GROUP BY TO_CHAR(created_at, 'YYYY-MM') " +
                   "ORDER BY mes ASC", nativeQuery = true)
    List<Object[]> contarPorMes(@Param("fechaInicio") java.time.LocalDateTime fechaInicio);
}
