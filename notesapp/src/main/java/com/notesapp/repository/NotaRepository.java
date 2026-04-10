package com.notesapp.repository;

import com.notesapp.entity.Nota;
import com.notesapp.enums.EstadoNoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Long> {

    // Busca las notas de un usuario específico
    List<Nota> findByUsuarioId(Long usuarioId);

    // Busca notas de un usuario filtrando por estado
    List<Nota> findByUsuarioIdAndEstado(Long usuarioId, EstadoNoteType estado);

    // Buscar notas por título o contenido (búsqueda parcial, case insensitive)
    @Query("SELECT n FROM Nota n WHERE n.usuario.id = :usuarioId " +
            "AND (LOWER(n.titulo) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(n.contenido) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Nota> buscarNotas(@Param("usuarioId") Long usuarioId,
            @Param("query") String query);

    void deleteByUsuarioId(Long usuarioId);

    // Conteo por estado (para estadísticas del dashboard)
    long countByEstado(EstadoNoteType estado);

    // Conteo total de notas de un usuario (para la tabla de usuarios admin)
    long countByUsuarioId(Long usuarioId);

    // Notas creadas por mes — últimos N meses (para gráfica del dashboard)
    @Query(value = "SELECT TO_CHAR(fecha_creacion, 'YYYY-MM') AS mes, COUNT(*) AS cantidad " +
                   "FROM notas WHERE fecha_creacion >= :fechaInicio " +
                   "GROUP BY TO_CHAR(fecha_creacion, 'YYYY-MM') " +
                   "ORDER BY mes ASC", nativeQuery = true)
    List<Object[]> contarPorMes(@Param("fechaInicio") java.time.LocalDateTime fechaInicio);
}
